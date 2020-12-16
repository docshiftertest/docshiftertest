package com.docshifter.core.monitoring.mappings;

import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.entities.AbstractConfigurationItem;

public interface ConfigurationItemConverter
        <TEntity extends AbstractConfigurationItem, TDto extends AbstractConfigurationItemDto> {

    TDto convertToDto(TEntity entity);
    void convertToDto(TEntity entity, TDto dto);
    TEntity convertToEntity(TDto dto);
    void convertToEntity(TDto dto, TEntity entity);
}
