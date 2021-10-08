package com.docshifter.core.monitoring.utils;

import com.docshifter.core.task.Task;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.docshifter.core.monitoring.services.NotificationService;
import lombok.extern.log4j.Log4j2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
public abstract class Warning {
	public static final String GLOBAL_WARNING_IDENTIFIER = "WARNING_NOTIFICATION";
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String DEFAULT_GROUPING_MESSAGE = "Other notifications";

	private final String warningTypeDesc;
	private final String description;

	public String getWarningTypeDesc() {
		return warningTypeDesc;
	}

	public String getDescription() {
		return description;
	}

	protected Warning(String warningTypeDesc, String description) {
		this.warningTypeDesc = warningTypeDesc;
		this.description = description;
	}

	/**
	 * Extract Warnings from the messages on the task and organise them first by the Warning Class/Identifier, then by
	 * the Warning Type within the identifier.
	 * @param task
	 * @return A Map of Map of Lists of warnings by identifier (e.g. AsposeWarning), then by type (e.g. FONT_SUBSTITUTION)
	 */
	public static Map<String, Map<String, List<Warning>>> warningsOnTask(Task task) {
		List<Warning> warnings = (List<Warning>)task.getData().get(GLOBAL_WARNING_IDENTIFIER);
		if (warnings == null) {
			return new HashMap<>();
		}

		return warnings.stream()
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.groupingBy(Warning::getIdentifier, Collectors.groupingBy(Warning::getWarningTypeDesc)));
	}

	/**
	 * Get a formatted list of all the Warnings in the Map
	 * @param warningsOnTask A Map of Map of Lists of warnings by identifier (e.g. AsposeWarning), then by type (e.g. FONT_SUBSTITUTION)
	 * @param htmlMarkup Whether to use rich HTML markup or plain text in the formatted message
	 * @return A String nicely formatted per warning identifier and type or an empty String
	 */
	// TODO: callers shouldn't have to pass htmlMarkup to this method. It should probably be the responsibility of the notification system
	//  to determine how the message should be formatted (e.g. webhook = plain text, mail = html).
	public static String getFormattedList(Map<String, Map<String, List<Warning>>> warningsOnTask, boolean htmlMarkup) {
		StringBuilder sBuf = new StringBuilder();
		String nl = htmlMarkup ? "<br>" : NEWLINE;
		for (Map.Entry<String, Map<String, List<Warning>>> warningsForIdentifier : warningsOnTask.entrySet()) {
			if (htmlMarkup) {
				sBuf.append("<b>");
			}
			sBuf.append("[")
					.append(warningsForIdentifier.getKey())
					.append("]");
			if (htmlMarkup) {
				sBuf.append("</b>");
			}
			sBuf.append(nl);
			for (Map.Entry<String, List<Warning>> warningsForGrouping : warningsForIdentifier.getValue().entrySet()) {
				boolean firstOfGroup = true;
				for (Warning warning : warningsForGrouping.getValue()) {
					if (warning == null) {
						continue;
					}

					if (firstOfGroup) {
						sBuf.append(warning.getGroupingMessage())
								.append(" (")
								.append(warningsForGrouping.getKey())
								.append("):");
						if (htmlMarkup) {
							sBuf.append("<ul>");
						} else {
							sBuf.append(nl);
						}
						firstOfGroup = false;
					}
					if (htmlMarkup) {
						sBuf.append("<li>");
					} else {
						sBuf.append("- ");
					}
					sBuf.append(warning.getDescription());
					if (htmlMarkup) {
						sBuf.append("</li>");
					} else {
						sBuf.append(nl);
					}
				}
				if (!firstOfGroup) {
					if (htmlMarkup) {
						sBuf.append("</ul>");
					} else {
						sBuf.append(nl);
					}
				}
			}
			sBuf.append(nl);
		}
		return sBuf.toString();
	}

	/**
	 * Get a formatted list of warnings for a specific warning identifier (e.g. AsposeWarning)
	 * @param warningsOnTask A Map of Map of Lists of warnings by identifier (e.g. AsposeWarning), then by type (e.g. FONT_SUBSTITUTION)
	 * @param identifier The identifier to filter on
	 * @param htmlMarkup Whether to use rich HTML markup or plain text in the formatted message
	 * @return A String nicely formatted per warning identifier and type or an empty String
	 */
	public static String getFormattedList(Map<String, Map<String, List<Warning>>> warningsOnTask, String identifier, boolean htmlMarkup) {
		Map<String, Map<String, List<Warning>>> singleWarningsOnTask = new HashMap<>();
		if (warningsOnTask.containsKey(identifier)) {
			singleWarningsOnTask.put(identifier, warningsOnTask.get(identifier));
		}
		return getFormattedList(singleWarningsOnTask, htmlMarkup);
	}

	/**
	 * Get a formatted list of warnings for a specific warning identifier (e.g. AsposeWarning) and warning type (e.g. FONT_SUBSTITUTION)
	 * @param warningsOnTask A Map of Map of Lists of warnings by identifier (e.g. AsposeWarning), then by type (e.g. FONT_SUBSTITUTION)
	 * @param identifier The identifier to filter on
	 * @param warningType The type to filter on
	 * @param htmlMarkup Whether to use rich HTML markup or plain text in the formatted message
	 * @return A String nicely formatted per warning identifier and type or an empty String
	 */
	public static String getFormattedList(Map<String, Map<String, List<Warning>>> warningsOnTask, String identifier, String warningType, boolean htmlMarkup) {
		Map<String, Map<String, List<Warning>>> singleWarningsOnTask = new HashMap<>();
		if (warningsOnTask.containsKey(identifier) &&
				warningsOnTask.get(identifier).containsKey(warningType)) {
			Map<String, List<Warning>> single = new HashMap<>();
			single.put(warningType, warningsOnTask.get(identifier).get(warningType));
			singleWarningsOnTask.put(identifier, single);
		}
		return getFormattedList(singleWarningsOnTask, htmlMarkup);
	}

	/**
	 * Convenience method to send a Warning Notification
	 * @param task
	 * @param warningInfo
	 * @param sourceFilePath The path pointing to the file to generate the notification for
	 * @param notificationConfigId
	 */
	public static void sendWarningNotification(Task task, String warningInfo, String sourceFilePath,
											   long notificationConfigId) {
		sendNotification(task, warningInfo, sourceFilePath, notificationConfigId, NotificationLevels.WARN);
	}

	/**
	 * Convenience method to send an Error Notification
	 * @param task
	 * @param warningInfo
	 * @param sourceFilePath The path pointing to the file to generate the notification for
	 * @param notificationConfigId
	 */
	public static void sendErrorNotification(Task task, String warningInfo, String sourceFilePath,
											 long notificationConfigId) {
		sendNotification(task, warningInfo, sourceFilePath, notificationConfigId, NotificationLevels.ERROR);
	}

	/**
	 * Convenience method to send a Notification
	 * @param task
	 * @param warningInfo
	 * @param sourceFilePath The path pointing to the file to generate the notification for
	 * @param notificationConfigId
	 * @param level
	 */
	private static void sendNotification(Task task, String warningInfo, String sourceFilePath,
										 long notificationConfigId, NotificationLevels level) {
		NotificationService notifier = (NotificationService) task.getData().get("notificationService");
		NotificationDto notification = new NotificationDto();
		notification.setLevel(level);
		notification.setMessage(warningInfo);
		notification.setTaskId(task.getId());
		notification.setSourceFilePath(sourceFilePath);
		notifier.sendNotification(notificationConfigId, notification);

		// Perform cleanup
		List<Warning> warnings = (List<Warning>)task.getData().get(GLOBAL_WARNING_IDENTIFIER);
		if (warnings != null) {
			warnings.clear();
		}
	}

	public void serializeToTask(Task task) {
		List<Warning> warnings = (List<Warning>)task.getData().get(GLOBAL_WARNING_IDENTIFIER);
		if (warnings == null) {
			warnings = new ArrayList<>();
			task.getData().put(GLOBAL_WARNING_IDENTIFIER, warnings);
		}
		warnings.add(this);
	}

	public final String getGroupingMessage() {
		return getGroupingMap().getOrDefault(warningTypeDesc, DEFAULT_GROUPING_MESSAGE);
	}

	/**
	 * Nicely format the String representation of this class
	 */
	@Override
	public String toString() {
		return new StringBuilder()
				.append(getIdentifier())
				.append(": {type=")
				.append(this.warningTypeDesc)
				.append(", description=")
				.append(this.description)
				.append("}")
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Warning warning = (Warning) o;
		return getIdentifier().equals(warning.getIdentifier()) &&
				warningTypeDesc.equals(warning.warningTypeDesc) &&
				description.equals(warning.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getIdentifier(), warningTypeDesc, description);
	}

	public abstract String getIdentifier();

	protected abstract Map<String, String> getGroupingMap();
}
