package com.docshifter.core.config.services;

import com.docshifter.core.config.conditions.IsInAnyContainerCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Exports application availability events (liveness and readiness) as files. It is then very useful to monitor
 * the existence of these files within a container environment.
 * @see <a href="https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-application-availability-managing">https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-application-availability-managing</a>
 */
@Component
@Conditional(IsInAnyContainerCondition.class)
@Log4j2
public class ApplicationStateExporter {

	// TODO: allow this to be configurable through @Value instead of a hardcoded path so it can be enabled for a
	//  classical installation of DocShifter as well?
	private static final String BASE_PATH = "/opt/DocShifter/";
	private static final File readyFile = new File(BASE_PATH + "ready");
	private static final File healthyFile = new File(BASE_PATH + "healthy");

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
