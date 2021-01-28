package com.docshifter.core.monitoring.enums;

import javax.persistence.Cacheable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Created by blazejm on 12.05.2017.
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
