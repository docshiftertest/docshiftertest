package com.docshifter.core.config.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of different health events happening across the application. If there are no problems, application
 * state will be kept at Correct. As soon as any problems are reported however, application state will be set to Broken.
 */
@Service
@Log4j2
public class HealthManagementService {
	/**
	 * An enumeration of health events that can potentially be detrimental to application performance. Some events
	 * are distinct (they can either be active or not application-wide) whereas other types can be stacked up next to
	 * each other.
	 */
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
	private final Map<Event, Integer> eventOccurrenceMap = new HashMap<>();

	public HealthManagementService(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	/**
	 * Reports a health event. The application state will be set to Broken if this is necessary.
	 * @param event The event to report.
	 */
	public synchronized void reportEvent(Event event) {
		int eventCount = getEventCount(event);
		if (!event.isDistinct() || eventCount <= 0) {
			eventCount++;
			log.error("{}, so setting the application state to BROKEN! This event has now occurred {} time(s).",
					event.getDescription(),	eventCount);
			eventOccurrenceMap.put(event, eventCount);
			AvailabilityChangeEvent.publish(appContext, LivenessState.BROKEN);
		}
	}

	/**
	 * Returns how many unresolved events there are for a certain type. Distinct event types will always return 0 or 1.
	 * @param event The event type to check.
	 * @return The number of unresolved events.
	 */
	public synchronized int getEventCount(Event event) {
		return eventOccurrenceMap.getOrDefault(event, 0);
	}

	/**
	 * Resolves a health event. Updates the application state to Correct if there are no more outstanding events.
	 * @param event The event to mark as resolved.
	 */
	public synchronized void resolveEvent(Event event) {
		int eventCount = getEventCount(event);
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
			log.warn("An event ({}) was marked as resolved, but it somehow wasn't reported before!", event.name());
		}
	}
}
