package com.docshifter.core.monitoring.dtos;

import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.SnmpConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.List;

/**
 * Created by blazejm on 19.05.2017.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MailConfigurationItemDto.class, name = ConfigurationTypes.MAIL),
        @JsonSubTypes.Type(value = WebhookConfigurationItemDto.class, name = ConfigurationTypes.WEBHOOK),
        @JsonSubTypes.Type(value = SnmpConfigurationItemDto.class, name = ConfigurationTypes.SNMP),
        @JsonSubTypes.Type(value = DbConfigurationItemDto.class, name = ConfigurationTypes.DB)
})
@Data
public abstract class AbstractConfigurationItemDto {

    private Long id;

    private List<NotificationLevels> notificationLevels;

    private String snippets;

    private String snippetsCombination;

    private String operator;

    public abstract ConfigurationTypes getType();

}
