package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.ConfigurationDto;
import com.docshifter.core.monitoring.dtos.ConfigurationItemDto;

import java.util.List;

/**
 * Created by blazejm on 11.05.2017.
 */
public interface ConfigurationService {
    ConfigurationDto getById(long id);

    List<ConfigurationDto> getAll();

    ConfigurationDto addConfiguration(ConfigurationDto configuration);

    ConfigurationDto updateConfiguration(long id, ConfigurationDto configuration);

    List<ConfigurationItemDto> getConfigurationItems(long configurationId);

    void deleteConfiguration(long id);
}
