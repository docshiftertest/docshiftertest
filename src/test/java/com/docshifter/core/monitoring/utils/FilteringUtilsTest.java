package com.docshifter.core.monitoring.utils;


import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class FilteringUtilsTest {

    private static final String ERROR_MESSAGE = "ERROR: the font is not valid for some reason that is really important for Docshifter!?   ";
    private static final String WARN_MESSAGE = "WARNING: the font is not valid for some reason that is really important for Docshifter.";
    private static final String SUCCESS_MESSAGE = "SUCCESS: the font is valid for some reason that is really important for Docshifter.";

    /**
     * Tests for notification filtering parameters
     */
    @ParameterizedTest
    @MethodSource("argumentsFiltering")
    public void testFilteringOptionsForNotification(
            String operator, String snippets, String snippetsCombination, String message, NotificationLevels notificationLevel, boolean expected) {

        AbstractConfigurationItemDto item = new AbstractConfigurationItemDto() {
            @Override
            public ConfigurationTypes getType() {
                return null;
            }
        };

        List<NotificationLevels> notificationLevels = new ArrayList<>();
        notificationLevels.add(notificationLevel);

        item.setOperator(operator);
        item.setSnippets(snippets);
        item.setSnippetsCombination(snippetsCombination);
        item.setNotificationLevels(notificationLevels);

        boolean result = FilteringUtils.checkIfShouldSendNotificationByFilter("someString", message, item);

        assertEquals(expected, result, "The result should match with the expected value");
    }

    private static Stream<Arguments> argumentsFiltering() {
        return Stream.of(
                // CONTAINS
                arguments("contains", "font",                   "or", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("contains", "font;docshifter",        "or", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("contains", "font",                   "and", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("contains", "font;docshifter",        "and", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("contains", "font;security",          "or", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("contains", "font;security",          "and", ERROR_MESSAGE, NotificationLevels.ERROR, false),
                arguments("contains", "security",               "or", ERROR_MESSAGE, NotificationLevels.ERROR, false),

                arguments("contains", "font;docshifter",        "or", SUCCESS_MESSAGE, NotificationLevels.SUCCESS, true),
                arguments("contains", "security",               "or", SUCCESS_MESSAGE, NotificationLevels.SUCCESS, false),

                arguments("contains", "font;docshifter",        "or", WARN_MESSAGE, NotificationLevels.WARN, true),
                arguments("contains", "security",               "or", WARN_MESSAGE, NotificationLevels.WARN, false),

                // STARTS WITH
                arguments("startsWith", "the font",             "or", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("startsWith", "the font",             "and", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("startsWith", "the font;error",       "or", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("startsWith", "error;the font",       "and", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("startsWith", "error",                "or", ERROR_MESSAGE, NotificationLevels.ERROR, false),
                arguments("startsWith", "error",                "and", ERROR_MESSAGE, NotificationLevels.ERROR, false),

                arguments("startsWith", "the font",             "and", WARN_MESSAGE, NotificationLevels.WARN, true),
                arguments("startsWith", "the font;error",       "or", WARN_MESSAGE, NotificationLevels.WARN, true),

                // ENDS WITH
                arguments("endsWith", "docshifter",             "or", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("endsWith", "docshifter",             "and", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("endsWith", "docshifter;the font",    "or", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("endsWith", "error;docshifter",       "and", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("endsWith", "font",                   "or", ERROR_MESSAGE, NotificationLevels.ERROR, false),
                arguments("endsWith", "font",                   "and", ERROR_MESSAGE, NotificationLevels.ERROR, false),

                // MATCHES
                arguments("matches",
                        "error: the font is not valid for some reason that is really important for Docshifter",
                        "or", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("matches",
                        "error: the Font is not valid for some reason that is REALLY important for docshifter",
                        "and", ERROR_MESSAGE, NotificationLevels.ERROR, true),
                arguments("matches",
                        "warning: the font is not valid for some reason that is really important for Docshifter",
                        "or", WARN_MESSAGE, NotificationLevels.WARN, true),
                arguments("matches",
                        "warn: the Font is not valid for some reason that is REALLY important for docshifter",
                        "and", WARN_MESSAGE, NotificationLevels.WARN, false),
                arguments("matches", "docshifter;the font",     "or", ERROR_MESSAGE, NotificationLevels.ERROR, false),
                arguments("matches", "error;docshifter",        "and", ERROR_MESSAGE, NotificationLevels.ERROR, false),
                arguments("matches", "font",                    "or", ERROR_MESSAGE, NotificationLevels.ERROR, false),
                arguments("matches", "font",                    "and", ERROR_MESSAGE, NotificationLevels.ERROR, false)
        );
    }



}