package com.docshifter.core.monitoring.mappings;

import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.entities.DbConfigurationItem;
import com.docshifter.core.monitoring.entities.MonitoringFilter;
import org.springframework.stereotype.Component;

@Component
public class DbConfigurationItemConverter implements ConfigurationItemConverter<DbConfigurationItem, DbConfigurationItemDto> {
    public DbConfigurationItemDto convertToDto(DbConfigurationItem entity) {
        if (entity == null) {
            return null;
        }
        DbConfigurationItemDto dto = new DbConfigurationItemDto();
        convertToDto(entity, dto);
        return dto;
    }

    public void convertToDto(DbConfigurationItem entity, DbConfigurationItemDto dto) {
        dto.setId(entity.getId());
        dto.setNotificationLevels(CommonConverter.convertToList(entity.getNotificationLevels()));
        dto.setConnection(entity.getConnection());
        dto.setDriver(entity.getDriver());
        dto.setDbUser(entity.getDbUser());
        dto.setPassword(entity.getPassword());
        dto.setTableName(entity.getTableName());
        if(entity.getMonitoringFilter() != null){
            dto.setOperator(entity.getMonitoringFilter().getOperator());
            dto.setSnippets(entity.getMonitoringFilter().getSnippets());
            dto.setSnippetsCombination(entity.getMonitoringFilter().getSnippetsCombination());
        }
    }

    public DbConfigurationItem convertToEntity(DbConfigurationItemDto dto) {
        if (dto == null) {
            return null;
        }
        DbConfigurationItem entity = new DbConfigurationItem();
        convertToEntity(dto, entity);
        return entity;
    }

    public void convertToEntity(DbConfigurationItemDto dto, DbConfigurationItem entity) {
        entity.setId(dto.getId());
        entity.setNotificationLevels(CommonConverter.convertToSet(dto.getNotificationLevels()));
        entity.setConnection(dto.getConnection());
        entity.setDriver(dto.getDriver());
        entity.setDbUser(dto.getDbUser());
        entity.setPassword(dto.getPassword());
        entity.setTableName(dto.getTableName());

        CommonConverter.convertFilterToEntity(dto,entity);
    }
}
