package com.docshifter.core.config.service;

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
@Conditional(IsInDockerCondition.class)
public class ApplicationStateExporter {

	private static final String BASE_PATH = "/opt/DocShifter/";

	@EventListener
	public void onReadinessStateChange(AvailabilityChangeEvent<ReadinessState> event) throws IOException {
		File file = new File(BASE_PATH + "ready");
		switch (event.getState()) {
			// Doesn't necessarily mean network traffic, just means it's analogous to the fact that the application
			// has started up and is ready to start doing actual processing
			// Also see https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-application-availability-readiness-state
			case ACCEPTING_TRAFFIC:
				file.createNewFile();
				break;
			// Ditto, doesn't necessarily mean network traffic, just means it's analogous to the fact that the
			// application is not (or no longer) ready yet to do any processing (before application and command-line
			// runners have been called).
			// Also see https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-application-availability-readiness-state
			case REFUSING_TRAFFIC:
				file.delete();
				break;
		}
	}

	@EventListener
	public void onLivenessStateChange(AvailabilityChangeEvent<LivenessState> event) throws IOException {
		File file = new File(BASE_PATH + "healthy");
		switch (event.getState()) {
			case CORRECT:
				file.createNewFile();
				break;
			case BROKEN:
				file.delete();
				break;
		}
	}
}
