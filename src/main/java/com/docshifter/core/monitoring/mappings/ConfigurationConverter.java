package com.docshifter.core.monitoring.mappings;

import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.ConfigurationDto;
import com.docshifter.core.monitoring.dtos.ConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.SnmpConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;
import com.docshifter.core.monitoring.entities.AbstractConfigurationItem;
import com.docshifter.core.monitoring.entities.Configuration;
import com.docshifter.core.monitoring.entities.DbConfigurationItem;
import com.docshifter.core.monitoring.entities.MailConfigurationItem;
import com.docshifter.core.monitoring.entities.SnmpConfigurationItem;
import com.docshifter.core.monitoring.entities.WebhookConfigurationItem;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class ConfigurationConverter implements Serializable {
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
                .map(this::convertToDto)
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
                .map(this::convertItemToDto)
                .collect(Collectors.toList());
    }

    public AbstractConfigurationItemDto convertItemToDto(AbstractConfigurationItem entity) {
        if (entity instanceof MailConfigurationItem item) {
            return mailConfigurationItemConverter.convertToDto(item);
        } else if (entity instanceof WebhookConfigurationItem item) {
            return webhookConfigurationItemConverter.convertToDto(item);
        } else if (entity instanceof SnmpConfigurationItem item) {
            return snmpConfigurationItemConverter.convertToDto(item);
        } else if (entity instanceof DbConfigurationItem item) {
            return dbConfigurationItemConverter.convertToDto(item);
        }
        return null;
    }

    public void convertItemToDto(AbstractConfigurationItem entity, AbstractConfigurationItemDto dto) {
        if (entity instanceof MailConfigurationItem item && dto instanceof MailConfigurationItemDto itemDto) {
            mailConfigurationItemConverter.convertToDto(item, itemDto);
        } else if (entity instanceof WebhookConfigurationItem item  && dto instanceof WebhookConfigurationItemDto itemDto) {
            webhookConfigurationItemConverter.convertToDto(item, itemDto);
        } else if (entity instanceof SnmpConfigurationItem item && dto instanceof SnmpConfigurationItemDto itemDto) {
            snmpConfigurationItemConverter.convertToDto(item, itemDto);
        } else if (entity instanceof DbConfigurationItem item && dto instanceof DbConfigurationItemDto itemDto) {
            dbConfigurationItemConverter.convertToDto(item, itemDto);
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
                .map(this::convertItemToEntity)
                .collect(Collectors.toList());
    }

    public AbstractConfigurationItem convertItemToEntity(AbstractConfigurationItemDto dto) {
        if (dto instanceof MailConfigurationItemDto itemDto) {
            return mailConfigurationItemConverter.convertToEntity(itemDto);
        } else if (dto instanceof WebhookConfigurationItemDto itemDto) {
            return webhookConfigurationItemConverter.convertToEntity(itemDto);
        } else if(dto instanceof SnmpConfigurationItemDto itemDto) {
            return snmpConfigurationItemConverter.convertToEntity(itemDto);
        } else if (dto instanceof  DbConfigurationItemDto itemDto) {
            return dbConfigurationItemConverter.convertToEntity(itemDto);
        }
        return null;
    }

    public void convertItemToEntity(AbstractConfigurationItemDto dto, AbstractConfigurationItem entity) {
        if (entity instanceof MailConfigurationItem item && dto instanceof MailConfigurationItemDto itemDto) {
            mailConfigurationItemConverter.convertToEntity(itemDto, item);
        } else if (entity instanceof WebhookConfigurationItem item  && dto instanceof WebhookConfigurationItemDto itemDto) {
            webhookConfigurationItemConverter.convertToEntity(itemDto, item);
        } else if (entity instanceof SnmpConfigurationItem item && dto instanceof SnmpConfigurationItemDto itemDto) {
            snmpConfigurationItemConverter.convertToEntity(itemDto, item);
        } else if (entity instanceof DbConfigurationItem item && dto instanceof DbConfigurationItemDto itemDto) {
            dbConfigurationItemConverter.convertToEntity(itemDto, item);
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
                .map(NotificationLevels::toString)
                .collect(Collectors.joining(", "));
    }
}
