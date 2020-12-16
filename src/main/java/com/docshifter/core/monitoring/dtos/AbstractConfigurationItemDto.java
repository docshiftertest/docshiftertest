package com.docshifter.core.monitoring.dtos;

import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.SnmpConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * Created by blazejm on 19.05.2017.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MailConfigurationItemDto.class, name = ConfigurationTypes.MAIL),
        @JsonSubTypes.Type(value = WebhookConfigurationItemDto.class, name = ConfigurationTypes.WEBHOOK),
        @JsonSubTypes.Type(value = SnmpConfigurationItemDto.class, name = ConfigurationTypes.SNMP),
        @JsonSubTypes.Type(value = DbConfigurationItemDto.class, name = ConfigurationTypes.DB)
})
public abstract class AbstractConfigurationItemDto {
    private Long id;
    private List<NotificationLevels> notificationLevels;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<NotificationLevels> getNotificationLevels() {
        return notificationLevels;
    }

    public void setNotificationLevels(List<NotificationLevels> notificationLevels) {
        this.notificationLevels = notificationLevels;
    }

    public abstract ConfigurationTypes getType();

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
