package com.docshifter.core.monitoring.mappings;

import com.docshifter.core.monitoring.dtos.SnmpConfigurationItemDto;
import com.docshifter.core.monitoring.entities.MonitoringFilter;
import com.docshifter.core.monitoring.entities.SnmpConfigurationItem;
import org.springframework.stereotype.Component;

@Component
public class SnmpConfigurationItemConverter implements ConfigurationItemConverter<SnmpConfigurationItem, SnmpConfigurationItemDto> {
    public SnmpConfigurationItemDto convertToDto(SnmpConfigurationItem entity) {
        if (entity == null) {
            return null;
        }
        SnmpConfigurationItemDto dto = new SnmpConfigurationItemDto();
        convertToDto(entity, dto);
        return dto;
    }

    public void convertToDto(SnmpConfigurationItem entity, SnmpConfigurationItemDto dto) {
        dto.setId(entity.getId());
        dto.setNotificationLevels(CommonConverter.convertToList(entity.getNotificationLevels()));
        dto.setIpAddress(entity.getIpAddress());
        dto.setPort(entity.getPort());
        dto.setCommunity(entity.getCommunity());
        dto.setTrapOid(entity.getTrapOid());
        if(entity.getMonitoringFilter() != null){
            dto.setOperator(entity.getMonitoringFilter().getOperator());
            dto.setSnippets(entity.getMonitoringFilter().getSnippets());
            dto.setSnippetsCombination(entity.getMonitoringFilter().getSnippetsCombination());
        }
    }

    public SnmpConfigurationItem convertToEntity(SnmpConfigurationItemDto dto) {
        if (dto == null) {
            return null;
        }
        SnmpConfigurationItem entity = new SnmpConfigurationItem();
        convertToEntity(dto, entity);
        return entity;
    }

    public void convertToEntity(SnmpConfigurationItemDto dto, SnmpConfigurationItem entity) {
        entity.setId(dto.getId());
        entity.setNotificationLevels(CommonConverter.convertToSet(dto.getNotificationLevels()));
        entity.setIpAddress(dto.getIpAddress());
        entity.setPort(dto.getPort());
        entity.setCommunity(dto.getCommunity());
        entity.setTrapOid(dto.getTrapOid());

        CommonConverter.convertFilterToEntity(dto,entity);
    }
}
