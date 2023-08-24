package com.docshifter.core.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

public enum DocShifterTextCase {

    UNKNOWN(-1, "UNKNOWN"),
    ANY(0, "ANY"),
    TITLE(1, "TITLE"),
    UPPER(2, "UPPER"),
    LOWER(3, "LOWER"),
    KEEP_AS_IS(4, "KEEPASIS");

    private final int value;
    private final String caseName;

    DocShifterTextCase(int value, String caseName) {
        this.value = value;
        this.caseName = caseName;
    }

    public int getValue() {
        return value;
    }

    public String getCaseName() {
        return caseName;
    }

    /**
     * Gets a case for a text
     * @param text String to get the case
     * @return the int corresponding to the case according
     */
    public static int getCaseFromText(String text) {

        int caseFromText;

        if (StringUtils.isAllUpperCase(text)) {
            caseFromText = DocShifterTextCase.UPPER.getValue();
        }
        else if (StringUtils.isAllLowerCase(text)) {
            caseFromText = DocShifterTextCase.LOWER.getValue();
        }
        else if (isTitle(text)) {
            caseFromText = DocShifterTextCase.TITLE.getValue();
        }
        else {
            caseFromText = DocShifterTextCase.UNKNOWN.getValue();
        }

        return caseFromText;
    }

    /**
     * Changes the case of a text according to the {@link DocShifterTextCase} provided
     * @param originalPhrase the original text
     * @param docShifterTextCase the {@link DocShifterTextCase} to change to
     * @return the text with the appropriated case
     */
    public static String changeCase(String originalPhrase, DocShifterTextCase docShifterTextCase) {

        return switch (docShifterTextCase) {
            case TITLE -> changeToTitle(originalPhrase);
            case UPPER -> originalPhrase.toUpperCase();
            case LOWER -> originalPhrase.toLowerCase();
            case KEEP_AS_IS, ANY, UNKNOWN -> originalPhrase;
        };
    }

    /**
     * Changes the case of a text to Title
     * @param phrase text to change the case
     * @return the text with the case Title
     */
    public static String changeToTitle(String phrase) {
        return WordUtils.capitalizeFully(phrase);
    }

    /**
     * Checks if the text provided is in Title case
     * @param text text to check
     * @return if it is in Title case or not
     */
    private static boolean isTitle(String text) {

        // If it is empty, we don't need to check
        if (StringUtils.isBlank(text)) {
            return false;
        }

        // Splitting by spaces to check for each word
        for (String word : text.split(" ")) {

            // The first letter should be in uppercase
            if (StringUtils.isBlank(word)|| !Character.isUpperCase(word.charAt(0)) && !Character.isDigit(word.charAt(0))) {
                return false;
            }

            // Starting for the second letter
            for (int index = 1; index < word.length(); index++) {

                char letter = word.charAt(index);

                // If it is uppercase, and it is not a digit, it is not TITLE
                if (Character.isUpperCase(letter) && !Character.isDigit(letter)) {
                    return false;
                }
            }
        }

        return true;
    }
}
