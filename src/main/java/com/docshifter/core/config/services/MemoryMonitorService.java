package com.docshifter.core.config.services;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.TextStringBuilder;
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
import java.util.concurrent.ScheduledExecutorService;

@Service
@Log4j2
public class MemoryMonitorService {
	public MemoryMonitorService(ScheduledExecutorService scheduler, HealthManagementService healthManagementService) {
		MemoryPoolMXBean tenuredGen = ManagementFactory.getMemoryPoolMXBeans().stream()
				.filter(pool -> pool.getType() == MemoryType.HEAP)
				.filter(MemoryPoolMXBean::isUsageThresholdSupported)
				.filter(MemoryPoolMXBean::isCollectionUsageThresholdSupported)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Can't find tenured generation MemoryPoolMXBean"));
		logJVMInfo(tenuredGen);
		updateThresholds(tenuredGen, 0.8f);
		NotificationEmitter notificationEmitter = (NotificationEmitter) ManagementFactory.getMemoryMXBean();
		notificationEmitter.addNotificationListener((notification, handback) -> {
			if (MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.equals(notification.getType())) {
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
				log.warn("JVM cleanup suggestion complete. Usage = {}B, collection usage = {}B",
						tenuredGen.getUsage().getUsed(), tenuredGen.getCollectionUsage().getUsed());
				healthManagementService.reportEvent(HealthManagementService.Event.MEMORY_SHORTAGE);
			} else if (MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(notification.getType())) {
				log.warn("Memory usage threshold has exceeded configured threshold (usage = {}B, collection usage = " +
						"{}B! Will take action as soon as the memory collection usage threshold exceeds.", tenuredGen.getUsage().getUsed(),
						tenuredGen.getCollectionUsage().getUsed());
				// This should trigger right before MEMORY_COLLECTION_THRESHOLD_EXCEEDED, here we make sure the
				// thresholds are kept up to date if the maximum available bytes of the tenured generation memory
				// pool somehow grew.
				updateThresholds(tenuredGen, 0.8f);
			}
		}, null, null);
	}

	private void updateThresholds(MemoryPoolMXBean tenuredGen, float relativeThreshold) {
		long maxUsage = tenuredGen.getUsage().getMax();
		log.debug("Max usage is {}B, max collection usage is {}B", maxUsage, tenuredGen.getCollectionUsage().getMax());
		long absoluteThreshold = (long) (maxUsage * relativeThreshold);
		tenuredGen.setCollectionUsageThreshold(absoluteThreshold);
		tenuredGen.setUsageThreshold(absoluteThreshold);
		log.info("Memory thresholds set to {}B", absoluteThreshold);
	}

	private void logJVMInfo(MemoryPoolMXBean tenuredGen) {
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
