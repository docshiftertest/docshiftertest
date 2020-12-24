package com.docshifter.core.monitoring.enums;

/**
 * Created by blazejm on 12.05.2017.
 */
public enum NotificationLevels {
    ERROR("ERROR"), WARN("WARN"), DEBUG("DEBUG"), INFO("INFO"), SUCCESS("SUCCESS");

    private final String text;

    NotificationLevels(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
