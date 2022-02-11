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

	private final ScheduledExecutorService scheduler;
	private final HealthManagementService healthManagementService;
	private final float relativeMemoryThreshold;
	private final long lowMemoryInitialDelay;
	private final long lowMemoryDelay;
	private final int lowMemoryConsecutivePasses;
	private final MemoryPoolMXBean tenuredGen;

	public DiagnosticsService(ScheduledExecutorService scheduler,
							  HealthManagementService healthManagementService,
							  @Value("${diagnostics.enable-startup-log:true}") boolean enableStartupLog,
							  @Value("${diagnostics.memory-monitor.relative-threshold:0.95}") float relativeMemoryThreshold,
							  @Value("${diagnostics.memory-monitor.low-memory-initial-delay:10}") long lowMemoryInitialDelay,
							  @Value("${diagnostics.memory-monitor.low-memory-delay:5}") long lowMemoryDelay,
							  @Value("${diagnostics.memory-monitor.low-memory-consecutive-passes:12}") int lowMemoryConsecutivePasses) {
		this.scheduler = scheduler;
		this.healthManagementService = healthManagementService;
		this.relativeMemoryThreshold = relativeMemoryThreshold;
		this.lowMemoryInitialDelay = lowMemoryInitialDelay;
		this.lowMemoryDelay = lowMemoryDelay;
		this.lowMemoryConsecutivePasses = lowMemoryConsecutivePasses;
		tenuredGen = ManagementFactory.getMemoryPoolMXBeans().stream()
				.filter(pool -> pool.getType() == MemoryType.HEAP)
				.filter(MemoryPoolMXBean::isUsageThresholdSupported)
				.filter(MemoryPoolMXBean::isCollectionUsageThresholdSupported)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Can't find tenured generation MemoryPoolMXBean"));

		if (enableStartupLog) {
			logJVMInfo();
		}

		if (relativeMemoryThreshold > 0) {
			setupMemoryListeners();
		}
	}

	/**
	 * Configures the thresholds and listeners of the low memory warning system.
	 */
	private void setupMemoryListeners() {
		updateThresholds();
		NotificationEmitter notificationEmitter = (NotificationEmitter) ManagementFactory.getMemoryMXBean();
		notificationEmitter.addNotificationListener((notification, handback) -> {
			if (MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.equals(notification.getType())) {
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

				runningFuture = scheduler.scheduleWithFixedDelay(this::monitorMemory, lowMemoryInitialDelay,
						lowMemoryDelay, TimeUnit.SECONDS);
			} else if (MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(notification.getType())) {
				log.warn("Memory usage threshold has exceeded configured threshold (usage = {}B, collection usage = " +
								"{}B! Will take action as soon as the memory collection usage threshold exceeds.", tenuredGen.getUsage().getUsed(),
						tenuredGen.getCollectionUsage().getUsed());
				// This should trigger right before MEMORY_COLLECTION_THRESHOLD_EXCEEDED, here we make sure the
				// thresholds are kept up to date if the maximum available bytes of the tenured generation memory
				// pool somehow grew.
				updateThresholds();
			}
		}, null, null);
	}

	/**
	 * Polls memory usage and reports noteworthy events.
	 */
	private void monitorMemory() {
		long memoryUsage = tenuredGen.getUsage().getUsed();
		long absoluteThreshold = (long) (tenuredGen.getUsage().getMax() * relativeMemoryThreshold);
		log.debug("Memory usage is now at {}B, threshold is {}B", memoryUsage, absoluteThreshold);
		if (memoryUsage >= absoluteThreshold) {
			healthManagementService.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
			consecutiveChecksBelowThreshold = 0;
		} else if (++consecutiveChecksBelowThreshold >= lowMemoryConsecutivePasses) {
			consecutiveChecksBelowThreshold = 0;
			// Make sure to not log useless warnings if we've never even reported the memory shortage
			if (healthManagementService.getEventCount(HealthManagementService.Event.MEMORY_SHORTAGE) > 0) {
				healthManagementService.resolveEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
			} else {
				log.info("Memory usage seemed to return to normal levels consistently immediately after forced GC " +
						"attempt.");
			}
			runningFuture.cancel(false);
		}
	}

	/**
	 * Actualizes the memory thresholds of the memory pool bean.
	 */
	private void updateThresholds() {
		long maxUsage = tenuredGen.getUsage().getMax();
		log.debug("Max usage is {}B, max collection usage is {}B", maxUsage, tenuredGen.getCollectionUsage().getMax());
		long absoluteThreshold = (long) (maxUsage * relativeMemoryThreshold);
		tenuredGen.setCollectionUsageThreshold(absoluteThreshold);
		tenuredGen.setUsageThreshold(absoluteThreshold);
		log.info("Memory thresholds set to {}B", absoluteThreshold);
	}

	/**
	 * Logs useful information related to the JVM and environment. This includes memory, Java Cryptography Extensions
	 * (JCE), supported charsets, fonts and system properties.
	 */
	private void logJVMInfo() {
		TextStringBuilder sb = new TextStringBuilder();
		sb.appendln("=== Here is some information related to the JVM and environment ===");

		sb.appendln("--- MEMORY ---");
		MemoryUsage usage = tenuredGen.getUsage();
		sb.appendln("Usage: initial = %sB, committed = %sB, max = %sB", usage.getInit(), usage.getCommitted(), usage.getMax());
		MemoryUsage collUsage = tenuredGen.getCollectionUsage();
		sb.appendln("Collection usage: initial = %sB, committed = %sB, max = %sB", collUsage.getInit(),
				collUsage.getCommitted(), collUsage.getMax());

		sb.appendln("--- JCE ---");
		try {
			int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
			sb.appendln("Max AES: %d", maxKeyLen);
		}
		catch (NoSuchAlgorithmException nsay) {
			sb.appendln("AES: No such algorithm!");
		}
		try {
			int maxKeyLen = Cipher.getMaxAllowedKeyLength("PBEWITHACSHA512ANDAES_256");
			sb.appendln("Max PBEWITHACSHA512ANDAES_256: %d", maxKeyLen);
		}
		catch (NoSuchAlgorithmException nsay) {
			sb.appendln("PBEWITHHMACSHA512ANDAES_256: No such algorithm!");
		}

		sb.appendln("--- CHARSETS ---");
		sb.appendln("Default charset: %s", Charset.defaultCharset());
		sb.appendln("Available charsets:");
		Charset.availableCharsets().forEach((name, cSet) -> sb.appendln("  Name: [%s], Charset: [%s]", name, cSet));

		sb.appendln("--- FONTS ---");
		sb.appendln("All font names:");
		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for (int i = 0; i < fontNames.length; i++) {
			sb.appendln("  Font %d name: %s", i + 1, fontNames[i]);
		}
		sb.appendln("All fonts:");
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (int i = 0; i < fonts.length; i++) {
			sb.appendln("  Font %d: %s", i + 1, fonts[i]);
		}

		sb.appendln("--- SYSTEM PROPERTIES ---");
		Properties props = System.getProperties();
		for (Map.Entry<Object, Object> prop : props.entrySet()) {
			sb.appendln("%s = %s", prop.getKey(), prop.getValue());
		}

		log.info(sb);
	}
}
