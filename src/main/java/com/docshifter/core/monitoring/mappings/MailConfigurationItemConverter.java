package com.docshifter.core.monitoring.mappings;

import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.entities.MailConfigurationItem;
import com.docshifter.core.monitoring.entities.MailTemplate;
import com.docshifter.core.monitoring.entities.SmtpConfiguration;
import com.docshifter.core.monitoring.mappings.CommonConverter;
import com.docshifter.core.monitoring.mappings.ConfigurationItemConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class MailConfigurationItemConverter implements ConfigurationItemConverter<MailConfigurationItem, MailConfigurationItemDto> {
    public MailConfigurationItemDto convertToDto(MailConfigurationItem entity) {
        if (entity == null) {
            return null;
        }
        MailConfigurationItemDto dto = new MailConfigurationItemDto();
        convertToDto(entity, dto);
        return dto;
    }

    public void convertToDto(MailConfigurationItem entity, MailConfigurationItemDto dto) {
        dto.setId(entity.getId());
        dto.setNotificationLevels(CommonConverter.convertToList(entity.getNotificationLevels()));

        if (entity.getSmtpConfiguration() != null) {
            dto.setHost(entity.getSmtpConfiguration().getHost());
            dto.setPort(entity.getSmtpConfiguration().getPort());
            dto.setUsername(entity.getSmtpConfiguration().getUsername());
            dto.setPassword(entity.getSmtpConfiguration().getPassword());
            dto.setFromAddress(entity.getSmtpConfiguration().getFromAddress());
            dto.setSsl(entity.getSmtpConfiguration().isSsl());
        }

        dto.setToAddresses(convertToString(entity.getMailAddresses()));

        if (entity.getMailTemplates() != null && !entity.getMailTemplates().isEmpty()) {
            MailTemplate template = entity.getMailTemplates().get(0);
            dto.setTemplateTitle(template.getTitle());
            dto.setTemplateBody(template.getBody());
        }
    }

    private static String convertToString(List<String> mailAddresses) {
        return mailAddresses != null
                ? String.join(", ", mailAddresses)
                : "";
    }

    public MailConfigurationItem convertToEntity(MailConfigurationItemDto dto) {
        if (dto == null) {
            return null;
        }
        MailConfigurationItem entity = new MailConfigurationItem();
        convertToEntity(dto, entity);
        return entity;
    }

    public void convertToEntity(MailConfigurationItemDto dto, MailConfigurationItem entity) {
        entity.setId(dto.getId());
        entity.setNotificationLevels(CommonConverter.convertToSet(dto.getNotificationLevels()));

        if (entity.getSmtpConfiguration() == null) entity.setSmtpConfiguration(new SmtpConfiguration());
        entity.getSmtpConfiguration().setHost(dto.getHost());
        entity.getSmtpConfiguration().setPort(dto.getPort());
        entity.getSmtpConfiguration().setUsername(dto.getUsername());
        entity.getSmtpConfiguration().setPassword(dto.getPassword());
        entity.getSmtpConfiguration().setFromAddress(dto.getFromAddress());
        entity.getSmtpConfiguration().setSsl(dto.isSsl());

        if (entity.getMailAddresses() == null) entity.setMailAddresses(new ArrayList<>());
        convertMailAddresses(dto.getToAddressList(), entity.getMailAddresses());

        if (entity.getMailTemplates() == null) entity.setMailTemplates(new ArrayList<>());
        if (entity.getMailTemplates().isEmpty()) {
            MailTemplate template = new MailTemplate();
            template.setMailConfigurationItem(entity);
            entity.getMailTemplates().add(template);
        }
        entity.getMailTemplates().get(0).setTitle(dto.getTemplateTitle());
        entity.getMailTemplates().get(0).setBody(dto.getTemplateBody());
    }

    private void convertMailAddresses(List<String> fromList, List<String> toList) {
        if (fromList == null || toList == null) {
            return;
        }
        for (String from : fromList) {
            if (!toList.contains(from)) {
                toList.add(from);
            }
        }
        for(Iterator<String> i = toList.iterator(); i.hasNext();) {
            String to = i.next();
            if (!fromList.contains(to)) {
                i.remove();
            }
        }
    }

}
