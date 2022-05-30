package com.docshifter.core.config.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Exports application availability events (liveness and readiness) as files. It is then very useful to monitor
 * the existence of these files.
 * @see <a href="https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-application-availability-managing">https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-application-availability-managing</a>
 */
@Service
@ConditionalOnProperty(prefix = "applicationState", name = "exportPath")
@Log4j2
public class ApplicationStateExporter {

	private final File readyFile;
	private final File healthyFile;

	public ApplicationStateExporter(@Value("${applicationState.exportPath}") String exportPath) {
		readyFile = Paths.get(exportPath, "ready").toFile();
		healthyFile = Paths.get(exportPath, "healthy").toFile();
		// Make sure to clean up these indicator files whenever we exit the JVM (if we exit regularly and don't crash
		// at least)
		readyFile.deleteOnExit();
		healthyFile.deleteOnExit();
	}

	// Not @Async because we can't guarantee atomicity of these I/O operations (in case both an ACCEPTING_TRAFFIC and
	// REFUSING_TRAFFIC event fires at roughly the same time)
	@EventListener
	public void onReadinessStateChange(AvailabilityChangeEvent<ReadinessState> event) throws IOException {
		switch (event.getState()) {
			// Doesn't necessarily mean network traffic, just means it's analogous to the fact that the application
			// has started up and is ready to start doing actual processing
			// Also see https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-application-availability-readiness-state
			case ACCEPTING_TRAFFIC:
				readyFile.createNewFile();
				break;
			// Ditto, doesn't necessarily mean network traffic, just means it's analogous to the fact that the
			// application is not (or no longer) ready yet to do any processing (before application and command-line
			// runners have been called).
			// Also see https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-application-availability-readiness-state
			case REFUSING_TRAFFIC:
				readyFile.delete();
				break;
			default:
				log.warn("Unexpected state found: {}", event.getState().name());
		}
	}

	// Not @Async because we can't guarantee atomicity of these I/O operations (in case both a CORRECT and BROKEN
	// event fires at roughly the same time)
	@EventListener
	public void onLivenessStateChange(AvailabilityChangeEvent<LivenessState> event) throws IOException {
		switch (event.getState()) {
			case CORRECT:
				healthyFile.createNewFile();
				break;
			case BROKEN:
				healthyFile.delete();
				break;
			default:
				log.warn("Unexpected state found: {}", event.getState().name());
		}
	}
}
