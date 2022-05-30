package com.docshifter.core.config.services;

import com.sun.management.OperatingSystemMXBean;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.TextStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.management.NotificationEmitter;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides general diagnostic tools to help troubleshoot an application.
 */
@Service
@Log4j2
public class DiagnosticsService {
	private final Map<MemoryPoolMXBean, AtomicReference<Future<?>>> runningFutures;
	private final Map<MemoryPoolMXBean, Integer> consecutiveChecksBelowThresholdMap;
	private final AtomicBoolean runningGc;
	private final NotificationEmitter notificationEmitter;
	private final MemoryPoolMXBean[] tenuredGens;

	private final ScheduledExecutorService scheduler;
	private final HealthManagementService healthManagementService;
	private final float relativeMemoryThreshold;
	private final long lowMemoryDelay;
	private final int lowMemoryConsecutivePasses;

	public DiagnosticsService(ScheduledExecutorService scheduler,
							  HealthManagementService healthManagementService,
							  @Value("${diagnostics.enable-startup-log:true}") boolean enableStartupLog,
							  @Value("${diagnostics.memory-monitor.relative-threshold:0.95}") float relativeMemoryThreshold,
							  @Value("${diagnostics.memory-monitor.low-memory-delay:5}") long lowMemoryDelay,
							  @Value("${diagnostics.memory-monitor.low-memory-consecutive-passes:12}") int lowMemoryConsecutivePasses) {
		this.scheduler = scheduler;
		this.healthManagementService = healthManagementService;
		this.relativeMemoryThreshold = relativeMemoryThreshold;
		this.lowMemoryDelay = lowMemoryDelay;
		this.lowMemoryConsecutivePasses = lowMemoryConsecutivePasses;

		if (enableStartupLog) {
			log.info(getJVMInfo());
		} else {
			log.info("JVM & environment information startup log is disabled.");
		}

		if (relativeMemoryThreshold > 0) {
			runningFutures = new HashMap<>();
			consecutiveChecksBelowThresholdMap = new HashMap<>();
			runningGc = new AtomicBoolean();
			notificationEmitter = (NotificationEmitter) ManagementFactory.getMemoryMXBean();

			tenuredGens = ManagementFactory.getMemoryPoolMXBeans().stream()
					.filter(pool -> pool.getType() == MemoryType.HEAP)
					.filter(MemoryPoolMXBean::isUsageThresholdSupported)
					.filter(MemoryPoolMXBean::isCollectionUsageThresholdSupported)
					.toArray(MemoryPoolMXBean[]::new);
			if (tenuredGens.length <= 0) {
				throw new IllegalStateException("Can't find any tenured generation MemoryPoolMXBeans");
			}
		} else {
			runningFutures = null;
			consecutiveChecksBelowThresholdMap = null;
			runningGc = null;
			notificationEmitter = null;
			tenuredGens = null;
			log.info("Low memory warning system is disabled.");
		}
	}

	/**
	 * Defer setting up the memory listeners until after the application has fully started up, as we don't want any
	 * shenanigans such as forced garbage collections to take place during startup if we're already over the
	 * thresholds! Instead we'll perform a single check in the constructor during bean creation and just start
	 * monitoring memory if we're over the cap.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onAppReady() {
		for (MemoryPoolMXBean tenuredGen : tenuredGens) {
			setupMemoryListener(tenuredGen);
		}
	}

	/**
	 * Configures the thresholds and listeners of the low memory warning system for a specific memory pool.
	 */
	private void setupMemoryListener(MemoryPoolMXBean tenuredGen) {
		log.info("Configuring memory listener for following pool: {}", tenuredGen.getName());
		runningFutures.put(tenuredGen, new AtomicReference<>());
		consecutiveChecksBelowThresholdMap.put(tenuredGen, 0);

		// Right after startup: check if we're over the usage threshold as the emitter we're about to configure might
		// not already fire (on select JDK/JREs only) if that's the case! Check the usage because the collection
		// usage might still be 0B as the garbage collector possibly hasn't had a chance to run yet.
		updateThresholds(tenuredGen);
		log.debug("Memory usage of pool {} is now at {}B, threshold is {}B", tenuredGen.getName(), tenuredGen.getUsage().getUsed(),
				tenuredGen.getUsageThreshold());
		if (tenuredGen.isUsageThresholdExceeded()) {
			onCollectionUsageThresholdExceeded(tenuredGen);
		}

		notificationEmitter.addNotificationListener((notification, handback) -> {
			if (MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.equals(notification.getType())) {
				onCollectionUsageThresholdExceeded(tenuredGen);
			} else if (MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(notification.getType())) {
				onUsageThresholdExceeded(tenuredGen);
			}
		}, null, null);
	}

	/**
	 * This should trigger right before MEMORY_COLLECTION_THRESHOLD_EXCEEDED as collection usage is only measured
	 * right after a garbage collection, here we make sure the thresholds are kept up to date if the maximum
	 * available bytes of the tenured generation memory pool somehow grew. We also attempt to force
	 * garbage collection in case the threshold has exceeded but the memory pool didn't grow as a result (a sign that
	 * we have probably reached maximum heap usage).
	 * @param tenuredGen The old generation/tenured memory pool bean.
	 */
	private void onUsageThresholdExceeded(MemoryPoolMXBean tenuredGen) {
		log.warn("Memory usage threshold of pool {} has exceeded configured threshold of {}B (usage = {}B, " +
						"collection usage = {}B)! Will check if memory pool was resized in the meantime and will start" +
						" monitoring memory as soon as the memory collection usage threshold exceeds.", tenuredGen.getName(),
				tenuredGen.getUsageThreshold(), tenuredGen.getUsage().getUsed(), tenuredGen.getCollectionUsage().getUsed());
		if (!updateThresholds(tenuredGen) && runningGc.compareAndSet(false, true)) {
			log.warn("After updating thresholds of pool {}: the current memory pool didn't grow nor shrink compared " +
					"to last time it was checked! Will suggest the JVM to perform a garbage cleanup.", tenuredGen.getName());

			// This might or might not improve the situation as these calls are mainly suggestions to the JVM...
			// But trying something is better than doing nothing

			// This first call suggests the JVM to search for unreferenced objects and clean them up.
			// If the objects have finalizers then those will be added to the finalizer queue.
			System.gc();
			// Suggests the finalizer queue to be emptied, so the JVM might run finalize() methods of objects
			// that were cleaned up in the first call to the GC.
			System.runFinalization();
			// The second GC run attempts to get rid of any nasties produced as a result of running all the
			// finalizers.
			System.gc();

			log.warn("JVM cleanup suggestion complete. Usage = {}B, collection usage = {}B of pool {}",
					tenuredGen.getUsage().getUsed(), tenuredGen.getCollectionUsage().getUsed(), tenuredGen.getName());
			runningGc.set(false);
		} else {
			log.debug("Not forcing GC because thresholds of memory pool {} were updated due to resizing of " +
					"pool, or already running garbage collection at the moment (so won't run it again).", tenuredGen.getName());
		}
	}

	/**
	 * Whenever the collection usage threshold of the old generation/tenured memory pool exceeds: start polling
	 * memory usage frequently if we aren't already doing so. The fact that that specific pool is about to spill over
	 * is a telltale sign that the JVM is about to run out of heap memory, as the tenured pool is the one typically
	 * containing the longest lived objects in the JVM (and also objects that spilled over from pools holding younger
	 * generation objects).
	 * @param tenuredGen The old generation/tenured memory pool bean.
	 */
	private void onCollectionUsageThresholdExceeded(MemoryPoolMXBean tenuredGen) {
		if (runningFutures.get(tenuredGen).compareAndSet(null, scheduler.scheduleWithFixedDelay(
				() -> monitorMemory(tenuredGen), 0, lowMemoryDelay, TimeUnit.SECONDS))) {
			Runtime runtime = Runtime.getRuntime();
			log.warn("Memory collection usage of pool {} has exceeded configured threshold of {}B (usage = {}B, " +
							"collection usage = {}B)! Will now start polling memory usage of this pool. Global JVM " +
							"memory stats: free = {}B, total = {}B, max = {}B",
					tenuredGen.getName(), tenuredGen.getCollectionUsageThreshold(), tenuredGen.getUsage().getUsed(),
					tenuredGen.getCollectionUsage().getUsed(), runtime.freeMemory(), runtime.totalMemory(), runtime.maxMemory());
		}
	}

	/**
	 * Polls memory usage in a pool and reports noteworthy events.
	 * @param tenuredGen The old generation/tenured memory pool bean.
	 */
	private void monitorMemory(MemoryPoolMXBean tenuredGen) {
		Integer consecutiveChecksBelowThreshold = consecutiveChecksBelowThresholdMap.get(tenuredGen);
		Runtime runtime = Runtime.getRuntime();
		log.debug("Memory usage of pool {} is now at {}B, threshold is {}B. Global JVM memory stats: free = {}B, " +
						"total = {}B, max = {}B", tenuredGen.getName(), tenuredGen.getUsage().getUsed(),
				tenuredGen.getUsageThreshold(), runtime.freeMemory(), runtime.totalMemory(), runtime.maxMemory());
		if (tenuredGen.isUsageThresholdExceeded()) {
			if (healthManagementService.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, tenuredGen)) {
				consecutiveChecksBelowThresholdMap.put(tenuredGen, 0);
			} else {
				healthManagementService.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE, tenuredGen);
			}
		} else if (++consecutiveChecksBelowThreshold >= lowMemoryConsecutivePasses) {
			// Make sure to not log useless warnings if we've never even reported the memory shortage for this pool
			if (healthManagementService.containsData(HealthManagementService.Event.MEMORY_SHORTAGE, tenuredGen)) {
				healthManagementService.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE, tenuredGen);
			} else {
				log.info("Memory usage of pool {} seemed to return to normal levels consistently immediately after " +
						"threshold notification was received.", tenuredGen.getName());
			}
			consecutiveChecksBelowThresholdMap.put(tenuredGen, 0);
			runningFutures.get(tenuredGen).getAndSet(null).cancel(false);
		} else {
			consecutiveChecksBelowThresholdMap.put(tenuredGen, consecutiveChecksBelowThreshold);
		}
	}

	/**
	 * Actualizes the memory thresholds of the memory pool bean.
	 * @param tenuredGen The old generation/tenured memory pool bean.
	 * @return True if the thresholds were updated to a newer value, false otherwise.
	 */
	private boolean updateThresholds(MemoryPoolMXBean tenuredGen) {
		long oldThreshold = tenuredGen.getUsageThreshold();
		long maxUsage = tenuredGen.getUsage().getMax();
		log.debug("Max usage is {}B, max collection usage is {}B of pool {}. Current threshold set to {}B", maxUsage,
				tenuredGen.getCollectionUsage().getMax(), tenuredGen.getName(), oldThreshold);
		long absoluteThreshold = (long) (maxUsage * relativeMemoryThreshold);
		if (oldThreshold != absoluteThreshold) {
			tenuredGen.setCollectionUsageThreshold(absoluteThreshold);
			tenuredGen.setUsageThreshold(absoluteThreshold);
			log.info("Memory thresholds of pool {} set to {}B", tenuredGen.getName(), absoluteThreshold);
			return true;
		}
		return false;
	}

	/**
	 * Retrieves useful information related to the JVM and environment and collects it to a formatted {@link String}.
	 * This includes CPU, memory, Java Cryptography Extensions (JCE), supported charsets, fonts and system properties.
	 */
	public String getJVMInfo() {
		TextStringBuilder sb = new TextStringBuilder();
		Runtime runtime = Runtime.getRuntime();
		sb.appendln("=== Here is some information related to the JVM and environment ===");

		sb.appendln("--- CPU ---");
		sb.appendln("Available processors: %d", runtime.availableProcessors());

		sb.appendln("--- MEMORY ---");
		sb.appendln("JVM max = %dB, free = %dB, total = %dB", runtime.maxMemory(), runtime.freeMemory(), runtime.totalMemory());
		sb.appendln("Found %d memory pool(s).", ManagementFactory.getMemoryPoolMXBeans().size());
		int memoryPoolNum = 1;
		for (MemoryPoolMXBean memoryPool : ManagementFactory.getMemoryPoolMXBeans()) {
			sb.appendln("Memory pool %d: %s", memoryPoolNum++, memoryPool.getName());
			sb.appendln("  It's a pool of type: %s", memoryPool.getType());
			sb.appendln("  Is it valid: %s", memoryPool.isValid());
			sb.appendln("  Is usage threshold supported: %s", memoryPool.isUsageThresholdSupported());
			sb.appendln("  Is collection usage threshold supported: %s", memoryPool.isCollectionUsageThresholdSupported());
			if (memoryPool.getMemoryManagerNames().length > 0) {
				sb.appendln("  It has %d memory manager(s):", memoryPool.getMemoryManagerNames().length);
				for (String memoryManager : memoryPool.getMemoryManagerNames()) {
					sb.appendln("    %s", memoryManager);
				}
			} else {
				sb.appendln("  It has no memory managers.");
			}
			if (memoryPool.isValid()) {
				MemoryUsage usage = memoryPool.getUsage();
				if (usage != null) {
					sb.appendln("  Usage: initial = %dB, used = %dB, committed = %dB, max = %dB", usage.getInit(),
							usage.getUsed(), usage.getCommitted(), usage.getMax());
				} else {
					sb.appendln("  Usage: NULL!");
				}
				MemoryUsage collUsage = memoryPool.getCollectionUsage();
				if (collUsage != null) {
					sb.appendln("  Collection usage: initial = %dB, used = %dB, committed = %dB, max = %dB",
							collUsage.getInit(), collUsage.getUsed(), collUsage.getCommitted(), collUsage.getMax());
				} else {
					sb.appendln("  Collection usage: NULL!");
				}
				MemoryUsage peakUsage = memoryPool.getPeakUsage();
				if (peakUsage != null) {
					sb.appendln("  Peak usage: initial = %dB, used = %dB, committed = %dB, max = %dB",
							peakUsage.getInit(), peakUsage.getUsed(), peakUsage.getCommitted(), peakUsage.getMax());
				} else {
					sb.appendln("  Peak usage: NULL!");
				}
			}
		}

		sb.appendln("--- JCE ---");
		try {
			int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
			sb.appendln("Max AES key length: %d", maxKeyLen);
		}
		catch (NoSuchAlgorithmException nsay) {
			sb.appendln("AES: No such algorithm!");
		}
		try {
			int maxKeyLen = Cipher.getMaxAllowedKeyLength("PBEWITHACSHA512ANDAES_256");
			sb.appendln("Max PBEWITHACSHA512ANDAES_256 key length: %d", maxKeyLen);
		}
		catch (NoSuchAlgorithmException nsay) {
			sb.appendln("PBEWITHHMACSHA512ANDAES_256: No such algorithm!");
		}

		sb.appendln("--- CHARSETS ---");
		sb.appendln("Default charset: %s", Charset.defaultCharset());
		sb.appendln("Available charsets:");
		Set<String> charsetNames = Charset.availableCharsets().keySet();
		int charsetNum = 1;
		for (String name : charsetNames) {
			sb.appendln("  Charset %d: %s", charsetNum++, name);
		}

		sb.appendln("--- FONTS ---");
		sb.appendln("All font family names:");
		int fontFamilyNum = 1;
		for (String fontFamily : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
			sb.appendln("  Font family %d: %s", fontFamilyNum++, fontFamily);
		}
		sb.appendln("All fonts:");
		int fontNum = 1;
		for (Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
			sb.appendln("  Font %d: %s", fontNum++, font);
		}

		sb.appendln("--- SYSTEM PROPERTIES ---");
		Properties props = System.getProperties();
		for (Map.Entry<Object, Object> prop : props.entrySet()) {
			sb.appendln("  %s = %s", prop.getKey(), prop.getValue());
		}

		return sb.toString();
	}

	/**
	 *
	 * Retrieves useful information related to the JVM and the physical memory of the computer.
	 * @return A map with the memory Info (Physical and JVM)
	 */
	public Map<String, Long> getMemoryInfo(){
		Map<String, Long> memory = new HashMap<>();
		Runtime runtime = Runtime.getRuntime();
		OperatingSystemMXBean mbean = (com.sun.management.OperatingSystemMXBean)
				ManagementFactory.getOperatingSystemMXBean();

		memory.put("JVM max", runtime.maxMemory());
		memory.put("JVM free", runtime.freeMemory());
		memory.put("JVM total", runtime.totalMemory());
		memory.put("Memory pool(s)", (long) ManagementFactory.getMemoryPoolMXBeans().size());
		memory.put("Free physical memory", mbean.getFreePhysicalMemorySize());
		memory.put("Total physical memory", mbean.getTotalPhysicalMemorySize());

		return memory;
	}


	/**
	 * Generates a thread dump of the entire application, going down as deep as possible.
	 * @return A thread dump in a friendly stringified format.
	 */
	public String generateThreadDump() {
		return generateThreadDump(Integer.MAX_VALUE);
	}

	/**
	 * Generates a thread dump of the entire application, going down to a certain depth.
	 * @param depth The maximum depth to descend to.
	 * @return A thread dump in a friendly stringified format.
	 */
	public String generateThreadDump(int depth) {
		final TextStringBuilder sb = new TextStringBuilder();
		final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), depth);
		for (ThreadInfo threadInfo : threadInfos) {
			sb.append('"').append(threadInfo.getThreadName()).appendln('"');
			final Thread.State state = threadInfo.getThreadState();
			sb.append("   java.lang.Thread.State: ").appendln(state);
			final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
			for (final StackTraceElement stackTraceElement : stackTraceElements) {
				sb.append("        at ").appendln(stackTraceElement);
			}
			sb.appendNewLine();
		}
		return sb.toString();
	}
}
