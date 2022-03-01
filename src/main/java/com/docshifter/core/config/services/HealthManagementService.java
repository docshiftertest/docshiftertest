package com.docshifter.core.config.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		MEMORY_SHORTAGE(false, "Memory shortage has been detected");
		// TODO: whenever adding an event type that is distinct, make sure to update and unignore the unit tests!

		private final boolean distinct;
		private final String description;
	}

	private final ApplicationContext appContext;
	/**
	 * Map holding specific events with each event's data grouped per event type.
	 */
	private final Map<Event, Set<Object>> eventDataMap = new HashMap<>();
	/**
	 * Map holding the counts of generic events (with no data) grouped per event type.
	 */
	private final Map<Event, Integer> genericEventOccurrenceMap = new HashMap<>();

	public HealthManagementService(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	/**
	 * Reports a specific health event with a piece of data attached to identify it. The application state will be set
	 * to Broken if this is necessary.
	 * @param event The event type to report.
	 * @param data The data to attach to this health event, so it can be tracked later. All data for events of the
	 *                same type should be unique, otherwise it won't be treated as a separate instance.
	 */
	public synchronized void reportEvent(Event event, Object data) {
		int eventCount = getEventCount(event);
		if (!event.isDistinct() || eventCount <= 0) {
			boolean added = true;
			if (data == null) {
				genericEventOccurrenceMap.put(event, getGenericEventCount(event) + 1);
			} else {
				Set<Object> dataSet = eventDataMap.getOrDefault(event, new HashSet<>());
				if (dataSet.add(data)) {
					eventDataMap.put(event, dataSet);
				} else {
					log.debug("Event {} already contains data: {}", event, data);
					added = false;
				}
			}

			if (added) {
				log.error("{}, so setting the application state to BROKEN! This event has now occurred {} time(s).",
						event.getDescription(),	++eventCount);
				AvailabilityChangeEvent.publish(appContext, LivenessState.BROKEN);
			}
		} else {
			log.debug("Event {} is distinct and it's already been reported before!", event);
		}
	}

	/**
	 * Reports a generic health event of a certain type with no data attached to it. The application state will be set
	 * to Broken if this is necessary.
	 * @param event The event type to report.
	 */
	public synchronized void reportEvent(Event event) {
		reportEvent(event, null);
	}

	/**
	 * Checks if an unresolved event of some type has been logged with a specific piece of data attached to it.
	 * @param event The event type to consider.
	 * @param data The unique data attached to the event to look for.
	 * @return True if there currently is a matching unresolved event, false otherwise.
	 */
	public synchronized boolean containsData(Event event, Object data) {
		Set<Object> eventData = eventDataMap.get(event);
		if (eventData == null) {
			return false;
		}

		return eventData.contains(data);
	}

	/**
	 * Returns how many unresolved events there are for a certain type. Distinct event types will always return 0 or 1.
	 * @param event The event type to check.
	 * @return The number of unresolved events.
	 */
	public synchronized int getEventCount(Event event) {
		return getGenericEventCount(event) + getSpecificEventCount(event);
	}

	/**
	 * Returns how many unresolved generic events (events with no data) there are for a certain type.
	 * @param event The event type to check.
	 * @return The number of unresolved events.
	 */
	public synchronized int getGenericEventCount(Event event) {
		return genericEventOccurrenceMap.getOrDefault(event, 0);
	}

	/**
	 * Returns how many unresolved specific events (events with some data) there are for a certain type.
	 * @param event The event type to check.
	 * @return The number of unresolved events.
	 */
	public synchronized int getSpecificEventCount(Event event) {
		int numData = 0;
		Set<Object> eventData = eventDataMap.get(event);
		if (eventData != null) {
			numData = eventData.size();
		}
		return numData;
	}

	/**
	 * Resolves either a specific health event of a certain type with some data attached to it or a distinct health
	 * event of that type. Updates the application state to Correct if there are no more outstanding events.
	 * @param event The event type to mark as resolved.
	 * @param data The data attached to the event to look for. Is not taken into account if the event is of a
	 *                distinct type.
	 */
	public synchronized void resolveEvent(Event event, Object data) {
		int eventCount;
		// Make an exception for distinct events.
		if (event.isDistinct()) {
			if (getEventCount(event) <= 0) {
				log.warn("A distinct event ({}) was marked as resolved, but no event of this type was reported before!",
						event.name());
				return;
			}
			eventDataMap.put(event, new HashSet<>());
			genericEventOccurrenceMap.put(event, 0);
			eventCount = 0;
		} else if (data == null) {
			int genericEventCount = getGenericEventCount(event);
			if (genericEventCount <= 0) {
				log.warn("A generic event ({}) was marked as resolved, but it somehow wasn't reported before!",
						event.name());
				return;
			}
			genericEventOccurrenceMap.put(event, --genericEventCount);
			eventCount = genericEventCount + getSpecificEventCount(event);
		} else {
			Set<Object> dataSet = eventDataMap.getOrDefault(event, null);
			if (dataSet == null || !dataSet.remove(data)) {
				log.warn("A specific event ({}) was marked as resolved, but it somehow wasn't reported before! " +
						"Searched for following data: {}", event.name(), data);
				return;
			}
			eventCount = dataSet.size() + getGenericEventCount(event);
		}

		log.info("The previously reported event \"{}\" has now been marked as resolved, there is/are now {} event(s) " +
				"of this type being tracked.", event.getDescription(), eventCount);
		if (eventCount <= 0
				&& genericEventOccurrenceMap.values().stream().allMatch(count -> count <= 0)
				&& eventDataMap.values().stream().allMatch(set -> set == null || set.isEmpty())) {
			log.info("There are no more outstanding health events, so setting the application state to CORRECT!");
			AvailabilityChangeEvent.publish(appContext, LivenessState.CORRECT);
		}
	}

	/**
	 * Resolves either a generic health event of a certain type with no data attached to it or a distinct health
	 * event of that type. Updates the application state to Correct if there are no more outstanding events.
	 * @param event The event type to mark as resolved.
	 */
	public synchronized void resolveEvent(Event event) {
		resolveEvent(event, null);
	}
}
