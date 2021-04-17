package com.docshifter.core.config.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "applicationState.broken", name = "exitSeconds")
@Log4j2
public class HealthMonitorService {

	private final ScheduledExecutorService executorService;
	private final long exitSeconds;
	private ScheduledFuture<?> runningFuture;

	public HealthMonitorService(@Value("${applicationState.broken.exitSeconds}") long exitSeconds,
								ScheduledExecutorService executorService) {
		this.exitSeconds = exitSeconds;
		this.executorService = executorService;
	}

	@EventListener
	public void onLivenessStateChange(AvailabilityChangeEvent<LivenessState> event) {
		switch (event.getState()) {
			case CORRECT:
				if (runningFuture != null) {
					runningFuture.cancel(false);
					runningFuture = null;
					log.info("Application has RESTORED! It will no longer terminate.");
				}
				break;
			case BROKEN:
				if (runningFuture != null) {
					runningFuture.cancel(false);
				}
				log.warn("Application is BROKEN! It will terminate after {} seconds.", exitSeconds);
				runningFuture = executorService.schedule(() -> {
							log.fatal("We are still BROKEN after {} seconds, so TERMINATING application with error " +
									"code!", exitSeconds);
							System.exit(1);
						}, exitSeconds, TimeUnit.SECONDS);
				break;
			default:
				log.warn("Unexpected state found: {}", event.getState().name());
		}
	}
}
