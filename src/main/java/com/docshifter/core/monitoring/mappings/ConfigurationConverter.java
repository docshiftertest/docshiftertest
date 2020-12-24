package com.docshifter.core.monitoring.mappings;

import com.docshifter.core.monitoring.dtos.*;
import com.docshifter.core.monitoring.entities.*;
import com.docshifter.core.monitoring.mappings.DbConfigurationItemConverter;
import com.docshifter.core.monitoring.mappings.MailConfigurationItemConverter;
import com.docshifter.core.monitoring.mappings.SnmpConfigurationItemConverter;
import com.docshifter.core.monitoring.mappings.WebhookConfigurationItemConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ConfigurationConverter {
    @Autowired
    private MailConfigurationItemConverter mailConfigurationItemConverter;

    @Autowired
    private WebhookConfigurationItemConverter webhookConfigurationItemConverter;

    @Autowired
    private SnmpConfigurationItemConverter snmpConfigurationItemConverter;

    @Autowired
    private DbConfigurationItemConverter dbConfigurationItemConverter;

    public List<ConfigurationDto> convertToDtos(Iterable<Configuration> configurations) {
        return StreamSupport.stream(configurations.spliterator(), false)
                .map(c -> convertToDto(c))
                .collect(Collectors.toList());
    }

    public ConfigurationDto convertToDto(Configuration entity) {
        if (entity == null) {
            return null;
        }
        ConfigurationDto dto = new ConfigurationDto();
        convertToDto(entity, dto);
        return dto;
    }

    public void convertToDto(Configuration entity, ConfigurationDto dto) {
        if (entity == null) {
            return;
        }
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (dto.getConfigurationItems() == null) dto.setConfigurationItems(new ArrayList<>());
        convertItemsToComplexDtos(entity.getConfigurationItems(), dto.getConfigurationItems());
    }

    private void convertItemsToComplexDtos(List<AbstractConfigurationItem> entities, List<AbstractConfigurationItemDto> dtos) {
        for(AbstractConfigurationItem entity: entities) {
            AbstractConfigurationItemDto dto = dtos.stream()
                    .filter(d -> Objects.equals(d.getId(), entity.getId()))
                    .findFirst()
                    .orElse(null);
            if (dto != null) {
                convertItemToDto(entity, dto);
            } else {
                dto = convertItemToDto(entity);
                dtos.add(dto);
            }
        }
        for (Iterator<AbstractConfigurationItemDto> i = dtos.iterator(); i.hasNext(); ) {
            AbstractConfigurationItemDto dto = i.next();
            AbstractConfigurationItem entity = entities.stream()
                    .filter(e -> Objects.equals(e.getId(), dto.getId()))
                    .findFirst()
                    .orElse(null);
            if (entity == null) {
                i.remove();
            }
        }
    }

    public List<AbstractConfigurationItemDto> convertItemsToDtos(Iterable<AbstractConfigurationItem> configurationItems) {
        return StreamSupport.stream(configurationItems.spliterator(), false)
                .map(ci -> convertItemToDto(ci))
                .collect(Collectors.toList());
    }

    public AbstractConfigurationItemDto convertItemToDto(AbstractConfigurationItem entity) {
        if (entity instanceof MailConfigurationItem) {
            return mailConfigurationItemConverter.convertToDto((MailConfigurationItem) entity);
        } else if (entity instanceof WebhookConfigurationItem) {
            return webhookConfigurationItemConverter.convertToDto((WebhookConfigurationItem) entity);
        } else if (entity instanceof SnmpConfigurationItem) {
            return snmpConfigurationItemConverter.convertToDto((SnmpConfigurationItem) entity);
        } else if (entity instanceof DbConfigurationItem) {
            return dbConfigurationItemConverter.convertToDto((DbConfigurationItem) entity);
        }
        return null;
    }

    public void convertItemToDto(AbstractConfigurationItem entity, AbstractConfigurationItemDto dto) {
        if (entity instanceof MailConfigurationItem && dto instanceof MailConfigurationItemDto) {
            mailConfigurationItemConverter.convertToDto((MailConfigurationItem) entity, (MailConfigurationItemDto) dto);
        } else if (entity instanceof WebhookConfigurationItem  && dto instanceof WebhookConfigurationItemDto) {
            webhookConfigurationItemConverter.convertToDto((WebhookConfigurationItem) entity, (WebhookConfigurationItemDto) dto);
        } else if (entity instanceof SnmpConfigurationItem && dto instanceof SnmpConfigurationItemDto) {
            snmpConfigurationItemConverter.convertToDto((SnmpConfigurationItem) entity, (SnmpConfigurationItemDto) dto);
        } else if (entity instanceof DbConfigurationItem && dto instanceof DbConfigurationItemDto) {
            dbConfigurationItemConverter.convertToDto((DbConfigurationItem) entity, (DbConfigurationItemDto) dto);
        }
    }

    public Configuration convertToEntity(ConfigurationDto dto) {
        if (dto == null) {
            return null;
        }
        Configuration entity = new Configuration();
        convertToEntity(dto, entity);
        return entity;
    }

    public void convertToEntity(ConfigurationDto dto, Configuration entity) {
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        if (entity.getConfigurationItems() == null) entity.setConfigurationItems(new ArrayList<>());
        convertItemsToEntities(dto.getConfigurationItems(), entity.getConfigurationItems());
        entity.getConfigurationItems()
                .stream()
                .forEach(ci -> ci.setConfiguration(entity));
    }

    private void convertItemsToEntities(List<AbstractConfigurationItemDto> dtos, List<AbstractConfigurationItem> entites) {
        for(AbstractConfigurationItemDto dto: dtos) {
            AbstractConfigurationItem entity = entites.stream()
                    .filter(d -> d.getId() != null
                            && Objects.equals(d.getId(), dto.getId()))
                    .findFirst()
                    .orElse(null);
            if (entity != null) {
                convertItemToEntity(dto, entity);
            } else {
                entity = convertItemToEntity(dto);
                entites.add(entity);
            }
        }
        for (Iterator<AbstractConfigurationItem> i = entites.iterator(); i.hasNext(); ) {
            AbstractConfigurationItem entity = i.next();
            AbstractConfigurationItemDto dto = dtos.stream()
                    .filter(e -> Objects.equals(e.getId(), entity.getId()))
                    .findFirst()
                    .orElse(null);
            if (dto == null) {
                i.remove();
            }
        }
    }

    public List<AbstractConfigurationItem> convertItemsToEntities(List<AbstractConfigurationItemDto> configurationItems) {
        return configurationItems.stream()
                .map(ci -> convertItemToEntity(ci))
                .collect(Collectors.toList());
    }

    public AbstractConfigurationItem convertItemToEntity(AbstractConfigurationItemDto dto) {
        if (dto instanceof MailConfigurationItemDto) {
            return mailConfigurationItemConverter.convertToEntity((MailConfigurationItemDto) dto);
        } else if (dto instanceof WebhookConfigurationItemDto) {
            return webhookConfigurationItemConverter.convertToEntity((WebhookConfigurationItemDto) dto);
        } else if(dto instanceof SnmpConfigurationItemDto) {
            return snmpConfigurationItemConverter.convertToEntity((SnmpConfigurationItemDto) dto);
        } else if (dto instanceof  DbConfigurationItemDto) {
            return dbConfigurationItemConverter.convertToEntity((DbConfigurationItemDto) dto);
        }
        return null;
    }

    public void convertItemToEntity(AbstractConfigurationItemDto dto, AbstractConfigurationItem entity) {
        if (entity instanceof MailConfigurationItem && dto instanceof MailConfigurationItemDto) {
            mailConfigurationItemConverter.convertToEntity((MailConfigurationItemDto) dto, (MailConfigurationItem) entity);
        } else if (entity instanceof WebhookConfigurationItem  && dto instanceof WebhookConfigurationItemDto) {
            webhookConfigurationItemConverter.convertToEntity((WebhookConfigurationItemDto) dto, (WebhookConfigurationItem) entity);
        } else if (entity instanceof SnmpConfigurationItem && dto instanceof SnmpConfigurationItemDto) {
            snmpConfigurationItemConverter.convertToEntity((SnmpConfigurationItemDto) dto, (SnmpConfigurationItem) entity);
        } else if (entity instanceof DbConfigurationItem && dto instanceof DbConfigurationItemDto) {
            dbConfigurationItemConverter.convertToEntity((DbConfigurationItemDto) dto, (DbConfigurationItem) entity);
        }
    }

    public static List<ConfigurationItemDto> convertItemsToSimpleDtos(Iterable<AbstractConfigurationItemDto> items) {
        int no = 1;
        List<ConfigurationItemDto> result = new ArrayList<>();
        for (AbstractConfigurationItemDto item : items) {
            ConfigurationItemDto r = convertItemsToSimpleDtos(item);
            r.setNo(no++);
            result.add(r);
        }
        return result;
    }


    public static ConfigurationItemDto convertItemsToSimpleDtos(AbstractConfigurationItemDto item) {
        ConfigurationItemDto dto = new ConfigurationItemDto();
        dto.setId(item.getId());
        dto.setType(item.getType());
        dto.setLevels(getLevels(item));
        return dto;
    }

    public static String getLevels(AbstractConfigurationItemDto configItem) {
        if (configItem.getNotificationLevels() == null) {
            return "";
        }
        return configItem.getNotificationLevels()
                .stream()
                .map(nl -> nl.toString())
                .collect(Collectors.joining(", "));
    }

}
