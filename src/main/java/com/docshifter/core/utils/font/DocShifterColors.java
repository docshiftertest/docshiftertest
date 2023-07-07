package com.docshifter.core.utils.font;

public enum DocShifterColors {

    RED("Red", "#FF0000"),
    YELLOW("Yellow", "#FFFF00"),
    LIGHT_BLUE("Light blue", "#ADD8E6"),
    BLUE("Blue", "#0000FF"),
    DARK_BLUE("Dark blue", "#00008B"),
    ORANGE("Orange", "#FFA500"),
    REDARK_ORANGED("Dark orange", "#FF8C00"),
    LIGHT_GREEN("Light green", "#90EE90"),
    GREEN("Green", "#008000"),
    DARK_GREEN("Dark green", "#006400"),
    VIOLET("Violet", "#EE82EE"),
    INDIGO("Indigo", "#4B0082"),
    PURPLE("Purple", "#800080"),
    REBECCA_PURPLE("Rebeccapurple", "#663399"),
    PINK("Pink", "#FFC0CB"),
    WHITE("White", "#FFFFFF"),
    BLACK("Black", "#000000"),
    LIGHT_GREY("Light gray/grey", "#D3D3D3"),
    GREY("Gray/Grey", "#808080"),
    DARK_GREY("Dark gray/grey", "#A9A9A9"),
    CYAN("Cyan", "#00FFFF"),
    MAGENTA("Magenta", "#FF00FF"),
    BROWN("Brown", "#A52A2A");

    private final String colorName;
    private final String colorRgbCode;

    DocShifterColors(String colorName, String colorRgbCode) {
        this.colorName = colorName;
        this.colorRgbCode = colorRgbCode;
    }

    /**
     * Finds the color RGB color code by the colorProvided
     * @param colorProvided Strign that can contain the colorName or the colorRgbCode
     * @return the color RGB code or null
     */
    public static String findByColor(String colorProvided) {

        for (DocShifterColors value : values()) {

            if (colorProvided.equalsIgnoreCase(value.colorName) || colorProvided.equals(value.colorRgbCode)) {
                return value.colorRgbCode;
            }
        }

        return null;
    }

}
