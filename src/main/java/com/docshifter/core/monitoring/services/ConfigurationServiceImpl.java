package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.ConfigurationDto;
import com.docshifter.core.monitoring.dtos.ConfigurationItemDto;
import com.docshifter.core.monitoring.entities.Configuration;
import com.docshifter.core.monitoring.mappings.ConfigurationConverter;
import com.docshifter.core.monitoring.repo.ConfigurationRepository;
import com.docshifter.core.monitoring.services.ConfigurationService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by blazejm on 11.05.2017.
 */
@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {
    private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.ConfigurationServiceImpl.class.getName());

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private ConfigurationConverter configurationConverter;

    @Override
    public ConfigurationDto getById(long id) {
        log.info("getById: " + id);

        Optional<Configuration> entityOptional = configurationRepository.findById(id);
        if (!entityOptional.isPresent()) {
        	log.info("Could not find (a Monitoring) Configuration for ID: " + id);
        	return null;
        }
        return configurationConverter.convertToDto(entityOptional.get());
    }

    @Override
    public List<ConfigurationDto> getAll() {
        log.info("getAll");

        Iterable<Configuration> entities = configurationRepository.findAll();
        return configurationConverter.convertToDtos(entities);
    }

    @Override
    public ConfigurationDto addConfiguration(ConfigurationDto configuration) {
        log.info("addConfiguration");

        Configuration entity = configurationConverter.convertToEntity(configuration);
        entity = configurationRepository.save(entity);
        return configurationConverter.convertToDto(entity);
    }

    @Override
    public ConfigurationDto updateConfiguration(long id, ConfigurationDto configuration) {
        log.info("updateConfiguration for id: " + id);

        Optional<Configuration> entityOptional = configurationRepository.findById(id);
        if (!entityOptional.isPresent()) {
            return null;
        }
        Configuration entity = entityOptional.get();
        configurationConverter.convertToEntity(configuration, entity);
        entity = configurationRepository.save(entity);
        return configurationConverter.convertToDto(entity);
    }

    @Override
    public List<ConfigurationItemDto> getConfigurationItems(long configurationId) {
        log.info("getConfigurationItems for configurationId: " + configurationId);

        Optional<Configuration> entity = configurationRepository.findById(configurationId);
        if (!entity.isPresent()) {
            return new ArrayList<>();
        }
        List<AbstractConfigurationItemDto> dtos = configurationConverter.convertItemsToDtos(entity.get().getConfigurationItems());
        return ConfigurationConverter.convertItemsToSimpleDtos(dtos);
    }

    @Override
    public void deleteConfiguration(long id) {
        log.info("deleteConfiguration for id: " + id);
        configurationRepository.deleteById(id);
    }

}
