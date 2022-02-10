package com.docshifter.core.config.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Log4j2
public class HealthManagementService {
	@Getter
	@AllArgsConstructor
	public enum Event {
		TASK_STUCK(false, "A task appears to be stuck"),
		CRITICAL_MQ_ERROR(false, "A critical message queue error has occurred"),
		MEMORY_SHORTAGE(true, "Memory shortage has been detected");

		private final boolean distinct;
		private final String description;
	}

	private final ApplicationContext appContext;
	private final HashMap<Event, Integer> eventOccurrenceMap = new HashMap<>();

	public HealthManagementService(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	public void reportEvent(Event event) {
		int eventCount = eventOccurrenceMap.getOrDefault(event, 0);
		if (!event.isDistinct() || eventCount <= 0) {
			eventCount++;
			log.error("{}, so setting the application state to BROKEN! This event has now occurred {} time(s).",
					event.getDescription(),	eventCount);
			eventOccurrenceMap.put(event, eventCount);
			AvailabilityChangeEvent.publish(appContext, LivenessState.BROKEN);
		}
	}

	public void resolveEvent(Event event) {
		int eventCount = eventOccurrenceMap.getOrDefault(event, 0);
		if (eventCount > 0) {
			eventCount--;
			log.info("The previously reported event \"{}\" has now been marked as resolved, there is/are now {} event(s) " +
					"of this type being tracked.", event.getDescription(), eventCount);
			eventOccurrenceMap.put(event, eventCount);
			if (eventCount <= 0 && eventOccurrenceMap.values().stream().allMatch(count -> count <= 0)) {
				log.info("There are no more outstanding health events, so setting the application state to CORRECT!");
				AvailabilityChangeEvent.publish(appContext, LivenessState.CORRECT);
			}
		} else {
			log.warn("An event was marked as resolved, but it somehow wasn't reported before!");
		}
	}
}
