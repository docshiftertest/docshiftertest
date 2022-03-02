package com.docshifter.core.config.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@ConditionalOnProperty(prefix = "applicationState.broken", name = "exitSeconds")
@Log4j2
public class HealthMonitorService {

	private final ScheduledExecutorService executorService;
	private final long exitSeconds;
	private AtomicReference<ScheduledFuture<?>> runningFuture = new AtomicReference<>();

	public HealthMonitorService(@Value("${applicationState.broken.exitSeconds}") long exitSeconds,
								ScheduledExecutorService executorService) {
		this.exitSeconds = exitSeconds;
		this.executorService = executorService;
	}

	@Async
	@EventListener
	public void onLivenessStateChange(AvailabilityChangeEvent<LivenessState> event) {
		switch (event.getState()) {
			case CORRECT:
				ScheduledFuture<?> runningFuture = this.runningFuture.getAndSet(null);
				if (runningFuture != null) {
					runningFuture.cancel(false);
					log.info("Application has RESTORED! It will no longer terminate.");
				}
				break;
			case BROKEN:
				if (this.runningFuture.compareAndSet(null,
						executorService.schedule(this::exitAfterTimeout, exitSeconds, TimeUnit.SECONDS))) {
					log.warn("Application is BROKEN! It will terminate after {} seconds.", exitSeconds);
				}
				break;
			default:
				log.warn("Unexpected state found: {}", event.getState().name());
		}
	}

	private void exitAfterTimeout() {
		log.fatal("We are still BROKEN after {} seconds, so TERMINATING application with error " +
				"code!", exitSeconds);
		System.exit(1);
	}
}
