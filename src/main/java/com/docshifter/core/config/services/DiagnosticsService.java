package com.docshifter.core.config.services;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.TextStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.management.NotificationEmitter;
import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides general diagnostic tools to help troubleshoot an application.
 */
@Service
@Log4j2
public class DiagnosticsService {
	private Future<?> runningFuture;
	private int consecutiveChecksBelowThreshold;

	private final NotificationEmitter notificationEmitter;
	private final ScheduledExecutorService scheduler;
	private final HealthManagementService healthManagementService;
	private final float relativeMemoryThreshold;
	private final long lowMemoryInitialDelay;
	private final long lowMemoryDelay;
	private final int lowMemoryConsecutivePasses;

	public DiagnosticsService(ScheduledExecutorService scheduler,
							  HealthManagementService healthManagementService,
							  @Value("${diagnostics.enable-startup-log:true}") boolean enableStartupLog,
							  @Value("${diagnostics.memory-monitor.relative-threshold:0.95}") float relativeMemoryThreshold,
							  @Value("${diagnostics.memory-monitor.low-memory-initial-delay:10}") long lowMemoryInitialDelay,
							  @Value("${diagnostics.memory-monitor.low-memory-delay:5}") long lowMemoryDelay,
							  @Value("${diagnostics.memory-monitor.low-memory-consecutive-passes:12}") int lowMemoryConsecutivePasses) {
		notificationEmitter = (NotificationEmitter) ManagementFactory.getMemoryMXBean();
		this.scheduler = scheduler;
		this.healthManagementService = healthManagementService;
		this.relativeMemoryThreshold = relativeMemoryThreshold;
		this.lowMemoryInitialDelay = lowMemoryInitialDelay;
		this.lowMemoryDelay = lowMemoryDelay;
		this.lowMemoryConsecutivePasses = lowMemoryConsecutivePasses;

		if (enableStartupLog) {
			logJVMInfo();
		} else {
			log.info("JVM & environment information startup log is disabled.");
		}

		if (relativeMemoryThreshold > 0) {
			setupMemoryListeners();
		} else {
			log.info("Low memory warning system is disabled.");
		}
	}

	/**
	 * Configures the thresholds and listeners of the low memory warning system.
	 */
	private void setupMemoryListeners() {
		MemoryPoolMXBean tenuredGen = ManagementFactory.getMemoryPoolMXBeans().stream()
				.filter(pool -> pool.getType() == MemoryType.HEAP)
				.filter(MemoryPoolMXBean::isUsageThresholdSupported)
				.filter(MemoryPoolMXBean::isCollectionUsageThresholdSupported)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Can't find tenured generation MemoryPoolMXBean"));
		log.info("Configuring memory listener for following pool: {}", tenuredGen.getName());
		updateThresholds(tenuredGen);

		// At startup: check if we're over the threshold as the emitter we're about to configure won't already fire if
		// that's the case!
		long memoryUsage = tenuredGen.getUsage().getUsed();
		long absoluteThreshold = (long) (tenuredGen.getUsage().getMax() * relativeMemoryThreshold);
		log.debug("Memory usage is now at {}B, threshold is {}B", memoryUsage, absoluteThreshold);
		if (memoryUsage >= absoluteThreshold) {
			onCollectionUsageThresholdExceeded(tenuredGen);
		}

		notificationEmitter.addNotificationListener((notification, handback) -> {
			if (MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.equals(notification.getType())) {
				onCollectionUsageThresholdExceeded(tenuredGen);
			} else if (MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(notification.getType())) {
				log.warn("Memory usage threshold has exceeded configured threshold (usage = {}B, collection usage = " +
								"{}B! Will take action as soon as the memory collection usage threshold exceeds.", tenuredGen.getUsage().getUsed(),
						tenuredGen.getCollectionUsage().getUsed());
				// This should trigger right before MEMORY_COLLECTION_THRESHOLD_EXCEEDED, here we make sure the
				// thresholds are kept up to date if the maximum available bytes of the tenured generation memory
				// pool somehow grew.
				updateThresholds(tenuredGen);
			}
		}, null, null);
	}

	/**
	 * Whenever the collection usage threshold of the old generation/tenured memory pool exceeds: attempt to force
	 * garbage collection and start polling memory usage frequently if we aren't already doing so. The fact that that
	 * specific pool is about to spill over is a telltale sign that the JVM is about to run out of heap memory, as
	 * the tenured pool is the one typically containing the longest lived objects in the JVM (and also objects that
	 * spilled over from pools holding younger generation objects).
	 * @param tenuredGen The old generation/tenured memory pool bean.
	 */
	private void onCollectionUsageThresholdExceeded(MemoryPoolMXBean tenuredGen) {
		if (runningFuture != null) {
			return;
		}
		// Fill this with a placeholder value for now as we're going to run GC first which might take a while
		runningFuture = new CompletableFuture<>();

		// This might or might not improve the situation as these calls are mainly suggestions to the JVM...
		// But trying something is better than doing nothing
		log.warn("Memory collection usage has exceeded configured threshold (usage = {}B, collection usage = " +
						"{}B)! Will suggest the JVM to perform a garbage cleanup.", tenuredGen.getUsage().getUsed(),
				tenuredGen.getCollectionUsage().getUsed());
		// This first call suggests the JVM to search for unreferenced objects and clean them up.
		// If the objects have finalizers then those will be added to the finalizer queue.
		System.gc();
		// Suggests the finalizer queue to be emptied, so the JVM might run finalize() methods of objects
		// that were cleaned up in the first call to the GC.
		System.runFinalization();
		// The second GC run attempts to get rid of any nasties produced as a result of running all the
		// finalizers.
		System.gc();
		log.warn("JVM cleanup suggestion complete, will now start polling memory usage. Usage = {}B, " +
				"collection usage = {}B", tenuredGen.getUsage().getUsed(), tenuredGen.getCollectionUsage().getUsed());

		runningFuture = scheduler.scheduleWithFixedDelay(() -> monitorMemory(tenuredGen), lowMemoryInitialDelay,
				lowMemoryDelay, TimeUnit.SECONDS);
	}

	/**
	 * Polls memory usage in a pool and reports noteworthy events.
	 * @param tenuredGen The old generation/tenured memory pool bean.
	 */
	private void monitorMemory(MemoryPoolMXBean tenuredGen) {
		long memoryUsage = tenuredGen.getUsage().getUsed();
		long absoluteThreshold = (long) (tenuredGen.getUsage().getMax() * relativeMemoryThreshold);
		log.debug("Memory usage is now at {}B, threshold is {}B", memoryUsage, absoluteThreshold);
		if (memoryUsage >= absoluteThreshold) {
			healthManagementService.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
			consecutiveChecksBelowThreshold = 0;
		} else if (++consecutiveChecksBelowThreshold >= lowMemoryConsecutivePasses) {
			// Make sure to not log useless warnings if we've never even reported the memory shortage
			if (healthManagementService.getEventCount(HealthManagementService.Event.MEMORY_SHORTAGE) > 0) {
				healthManagementService.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
			} else {
				log.info("Memory usage seemed to return to normal levels consistently immediately after forced GC " +
						"attempt.");
			}
			runningFuture.cancel(false);
			runningFuture = null;
			consecutiveChecksBelowThreshold = 0;
		}
	}

	/**
	 * Actualizes the memory thresholds of the memory pool bean.
	 * @param tenuredGen The old generation/tenured memory pool bean.
	 */
	private void updateThresholds(MemoryPoolMXBean tenuredGen) {
		long maxUsage = tenuredGen.getUsage().getMax();
		log.debug("Max usage is {}B, max collection usage is {}B", maxUsage, tenuredGen.getCollectionUsage().getMax());
		long absoluteThreshold = (long) (maxUsage * relativeMemoryThreshold);
		tenuredGen.setCollectionUsageThreshold(absoluteThreshold);
		tenuredGen.setUsageThreshold(absoluteThreshold);
		log.info("Memory thresholds set to {}B", absoluteThreshold);
	}

	/**
	 * Logs useful information related to the JVM and environment. This includes CPU, memory, Java Cryptography
	 * Extensions (JCE), supported charsets, fonts and system properties.
	 */
	private void logJVMInfo() {
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

		log.info(sb);
	}
}
