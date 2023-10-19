package com.docshifter.core.utils;

public final class Constants {

    private Constants() {}

    /**
     * Special merge field name that represents a value received by the Cover Page module to internally keep track of
     * the volume number of a report.
     */
    public static final String VOLUME_NO_REPLACE = "$VOLUME_NUMBER";

    /**
     * Special merge field name that represents a value received by the Cover Page module to internally keep track of
     * the total number of volumes of a report.
     */
    public static final String VOLUME_TOT_REPLACE = "$VOLUME_TOTAL";

}
