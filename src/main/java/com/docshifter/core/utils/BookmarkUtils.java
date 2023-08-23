package com.docshifter.core.utils;

import com.docshifter.core.exceptions.InvalidConfigException;
import com.docshifter.core.utils.font.DocShifterFontStyling;

import java.util.Arrays;
import java.util.List;

public class BookmarkUtils {

    /**
     * Splits the string representing the bookmark levels provided by the customer separated by semicolon
     *
     * @param bookmarkLevel string to split
     * @param splitBy character to split the list
     * @return a list of bookmark levels
     * @throws InvalidConfigException if any level provided is not valid
     */
    public static List<Integer> parseBookmarkLevels(String bookmarkLevel, String splitBy) throws InvalidConfigException {

        return Arrays.stream(bookmarkLevel.split(splitBy))
                .map(level -> {
                    try {
                        return Integer.valueOf(level);
                    }
                    catch (NumberFormatException numberFormatException) {
                        throw new InvalidConfigException("It is not possible to convert the bookmarkLevel [" + level + "]. Please add a valid integer number.");
                    }
                })
                .toList();
    }

    /**
     * Splits the string representing the font style provided by the customer separated by semicolon
     *
     * @param styling list to split
     * @param splitBy character to split the list
     * @return a list of integer representing the styles
     * @throws InvalidConfigException if any style provided is not valid
     */
    public static List<DocShifterFontStyling> parseStyling(String styling, String splitBy, boolean useRegularAsDefault) throws InvalidConfigException {

        return Arrays.stream(styling.split(splitBy))
                .map(style -> switch (style.toLowerCase()) {
                    case "any"              -> DocShifterFontStyling.ANY;
                    case "regular"          -> DocShifterFontStyling.REGULAR;
                    case "bold"             -> DocShifterFontStyling.BOLD;
                    case "italic"           -> DocShifterFontStyling.ITALIC;
                    case "bolditalic",
                         "italicbold"       -> DocShifterFontStyling.BOLD_ITALIC;
                    case "underline"        -> DocShifterFontStyling.UNDERLINE;
                    default -> {

                        if (useRegularAsDefault) {
                            yield DocShifterFontStyling.REGULAR;
                        }

                        throw new InvalidConfigException("The style [" + style + "] is not valid.");
                    }
                })
                .toList();
    }


    /**
     * Splits the string representing the bookmark cases provided by the customer separated by semicolon
     *
     * @param bookmarkCase list to split
     * @param splitBy character to split the list
     * @return a list of integer representing the case choices
     */
    public static List<DocShifterTextCase> parseBookmarkCase(String bookmarkCase, String splitBy, boolean useKeepAsIsAsDefault) throws InvalidConfigException {

        return Arrays.stream(bookmarkCase.split(splitBy))
                .map(bookmark -> switch (bookmark.toLowerCase()) {
                    case "title" -> DocShifterTextCase.TITLE;
                    case "upper" -> DocShifterTextCase.UPPER;
                    case "lower" -> DocShifterTextCase.LOWER;
                    case "asinput" -> DocShifterTextCase.KEEP_AS_IS;
                    default -> {

                        if (useKeepAsIsAsDefault) {
                            yield DocShifterTextCase.KEEP_AS_IS;
                        }

                        throw new InvalidConfigException("The bookmarkCase [" + bookmark + "] is not valid. Please chose a valid option: [TITLE, UPPER, LOWER or ASINPUT]");
                    }
                })
                .toList();
    }

}
