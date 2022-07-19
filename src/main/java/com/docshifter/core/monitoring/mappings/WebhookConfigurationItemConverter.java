package com.docshifter.core.monitoring.mappings;

import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;
import com.docshifter.core.monitoring.entities.MonitoringFilter;
import com.docshifter.core.monitoring.entities.WebhookConfigurationItem;
import com.docshifter.core.monitoring.entities.WebhookTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class WebhookConfigurationItemConverter implements ConfigurationItemConverter<WebhookConfigurationItem, WebhookConfigurationItemDto> {
    public WebhookConfigurationItemDto convertToDto(WebhookConfigurationItem entity) {
        if (entity == null) {
            return null;
        }
        WebhookConfigurationItemDto dto = new WebhookConfigurationItemDto();
        convertToDto(entity, dto);
        return dto;
    }

    public void convertToDto(WebhookConfigurationItem entity, WebhookConfigurationItemDto dto) {
        dto.setId(entity.getId());
        dto.setNotificationLevels(
                CommonConverter.convertToList(entity.getNotificationLevels()));
        dto.setUrl(entity.getUrl());

        if (entity.getWebhookTemplates() != null && !entity.getWebhookTemplates().isEmpty()) {
            WebhookTemplate template = entity.getWebhookTemplates().get(0);
            dto.setBody(template.getBody());
            dto.setUrlParams(
                    CommonConverter.convertToKeyValuePairs(template.getUrlParams()));
            dto.setHeaderParams(
                    CommonConverter.convertToKeyValuePairs(template.getHeaderParams()));
        }

        if(entity.getMonitoringFilter() != null){
            dto.setOperator(entity.getMonitoringFilter().getOperator());
            dto.setSnippets(entity.getMonitoringFilter().getSnippets());
            dto.setSnippetsCombination(entity.getMonitoringFilter().getSnippetsCombination());
        }
    }

    public WebhookConfigurationItem convertToEntity(WebhookConfigurationItemDto dto) {
        if (dto == null) {
            return null;
        }
        WebhookConfigurationItem entity = new WebhookConfigurationItem();
        convertToEntity(dto, entity);
        return entity;
    }

    public void convertToEntity(WebhookConfigurationItemDto dto, WebhookConfigurationItem entity) {
        entity.setId(dto.getId());
        entity.setNotificationLevels(
                CommonConverter.convertToSet(dto.getNotificationLevels()));
        entity.setUrl(dto.getUrl());

        if (entity.getWebhookTemplates() == null) {
            entity.setWebhookTemplates(new ArrayList<>());
        }

        if (entity.getWebhookTemplates().isEmpty()) {
            WebhookTemplate template = new WebhookTemplate();
            template.setWebhookConfigurationItem(entity);
            entity.getWebhookTemplates().add(template);
        }

        CommonConverter.convertFilterToEntity(dto,entity);

        entity.getWebhookTemplates().get(0).setBody(dto.getBody());
        entity.getWebhookTemplates().get(0).setUrlParams(
                CommonConverter.convertToMap(dto.getUrlParams()));
        entity.getWebhookTemplates().get(0).setHeaderParams(
                CommonConverter.convertToMap(dto.getHeaderParams()));

    }
}
