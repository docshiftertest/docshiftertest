package com.docshifter.core.utils.font;

import com.aspose.words.Font;
import com.aspose.words.Underline;

public enum DocShifterFontStyling {

    ANY(-1),
    REGULAR(0),
    BOLD(1),
    ITALIC(2),
    UNDERLINE(3),
    BOLD_ITALIC(4);

    private final int value;

    DocShifterFontStyling(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * Validates of a {@link DocShifterFontStyling} option matches with the {@link Font} provided
     * @param styling the {@link DocShifterFontStyling} to be used
     * @param runNodeFont the {@link Font} to validate
     * @return if the {@link Font} provided has the {@link DocShifterFontStyling}
     */
    public static boolean validateStyling(DocShifterFontStyling styling, Font runNodeFont) {

        return (BOLD.equals(styling) && runNodeFont.getBold())
                || (ITALIC.equals(styling)  && runNodeFont.getItalic())
                || (UNDERLINE.equals(styling) && runNodeFont.getUnderline() != Underline.NONE)
                || (REGULAR.equals(styling)
                    && runNodeFont.getUnderline() == Underline.NONE
                    && !runNodeFont.getItalic()
                    && !runNodeFont.getBold());
    }
}
