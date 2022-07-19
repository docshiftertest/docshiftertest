package com.docshifter.core.monitoring.utils;

import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Log4j2
public class FilteringUtils {

    /**
     * Checks if the notifications should be sent according to the filtering options
     * @param taskId id of the task
     * @param message message to be checked
     * @param item item that contains the filtering options
     * @return if the notification should be sent or not
     */
    public static boolean checkIfShouldSendNotificationByFilter(String taskId, String message, AbstractConfigurationItemDto item) {

        List<String> snippets = splitToList(item.getSnippets());

        if (snippets.isEmpty()) {
            log.debug("The notification for the taskId [{}] will be sent as the snippets filter field is empty.",
                    taskId);
            return true;
        }

        String operator = item.getOperator();
        String snippetsCombination = item.getSnippetsCombination();
        List<NotificationLevels> notificationLevels = item.getNotificationLevels();

        boolean shouldSendNotification = true;

        log.debug("Check the notification for the taskId [{}] with filtering options, operator: {}, snippets: {}, snippetsCombination: {}",
                taskId, operator, snippets, snippetsCombination);

        String finalMessage;

        switch (operator) {
            case "contains" -> {

                finalMessage = message;

                if (snippetsCombination.equalsIgnoreCase("or")) {
                    shouldSendNotification = snippets.stream().anyMatch(
                            snippet -> finalMessage.toLowerCase().contains(snippet.toLowerCase()));
                }
                else {
                    shouldSendNotification = snippets.stream().allMatch(
                            snippet -> finalMessage.toLowerCase().contains(snippet.toLowerCase()));
                }
            }
            case "startsWith" -> {

                finalMessage = removeNotificationLevelFromBeginning(message, notificationLevels);

                shouldSendNotification = snippets.stream().anyMatch(
                        snippet -> finalMessage.toLowerCase().startsWith(snippet.toLowerCase()));
            }

            case "endsWith" -> {

                finalMessage = removePunctuationFromTheEnd(message);

                shouldSendNotification = snippets.stream().anyMatch(
                        snippet -> finalMessage.toLowerCase().endsWith(snippet.toLowerCase()));
            }

            case "matches" -> {

                message = removeNotificationLevelFromBeginning(message, notificationLevels);
                message = removePunctuationFromTheEnd(message);

                finalMessage = message;

                shouldSendNotification = snippets.stream().anyMatch(
                        snippet -> finalMessage.toLowerCase().matches(snippet.toLowerCase()));
            }
        }

        log.debug("shouldSendNotification is {}", shouldSendNotification);
        return shouldSendNotification;
    }

    /**
     * Splits the string provided by the customer by semicolon
     * @param inputFromCustomer that contains a list of strings separated by semicolons
     * @return a list of strings
     */
    private static List<String> splitToList(String inputFromCustomer) {

        if (StringUtils.isBlank(inputFromCustomer)) {
            return new ArrayList<>();
        }

        if (!inputFromCustomer.contains(";")) {
            return Collections.singletonList(inputFromCustomer);
        }

        return Arrays.asList(inputFromCustomer.split(";"));
    }

    /**
     * Removes the notification level from the beginning of the message
     * @param message message to be edited
     * @param notificationLevels list of notification levels set by the customer
     * @return the message without the notification level set by the customer
     */
    private static String removeNotificationLevelFromBeginning(String message, List<NotificationLevels> notificationLevels) {

        for (NotificationLevels notificationLevel : notificationLevels) {

            if (message.startsWith(notificationLevel.toString())) {

                if (message.startsWith("WARNING")) {
                    return message.replaceFirst(
                            "WARNING" + ": ", "");
                }

                return message.replaceFirst(
                        notificationLevel + ": ", "");
            }
        }

        return message;
    }

    /**
     * Check and remove punctuation or blank space from the end of a message
     * @param message message to be checked
     * @return the string without blank spaces or punctuations
     */
    private static String removePunctuationFromTheEnd(String message) {

        while (Pattern.compile("[.!?\\s]$").matcher(message).find()) {
            message = message.substring(0, message.length() - 1);
        }

        return message;
    }

}
