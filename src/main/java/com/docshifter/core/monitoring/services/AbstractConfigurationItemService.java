package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.docshifter.core.monitoring.entities.AbstractConfigurationItem;
import com.docshifter.core.monitoring.entities.Configuration;
import com.docshifter.core.monitoring.mappings.ConfigurationItemConverter;
import com.docshifter.core.monitoring.repo.ConfigurationRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Optional;

@Transactional
public abstract class AbstractConfigurationItemService
        <TEntity extends AbstractConfigurationItem, TDto extends AbstractConfigurationItemDto> {
    private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.AbstractConfigurationItemService.class.getName());

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    protected EntityManager entityManager;

    protected abstract ConfigurationItemConverter<TEntity,TDto> getConverter();

    public TDto getById(long configurationId, long id) {
        log.info("getById with Id: " + configurationId + " and id: " + id);

        Optional<Configuration> configurationOptional = getConfigurationById(configurationId);
        if (!configurationOptional.isPresent()) {
        	log.error("Could not get Configuration for Id: " + configurationId);
        	return null;
        }
        Configuration configuration = configurationOptional.get();
        TEntity entity = getConfigurationItemById(configuration, id);
        return getConverter().convertToDto(entity);
    }

    public TDto add(long configurationId, TDto dto) {
        log.info("add with Configuration Id: " + configurationId);

        Optional<Configuration> configurationOptional = getConfigurationById(configurationId);
        Configuration configuration;
        if (configurationOptional.isPresent()) {
        	configuration = configurationOptional.get();
        }
        else {
            configuration = new Configuration();
            configuration.setId(configurationId);
            configuration = configurationRepository.save(configuration);
        }

        TEntity entity = getConverter().convertToEntity(dto);
        entity.setConfiguration(configuration);
        if (configuration.getConfigurationItems() == null) {
            configuration.setConfigurationItems(new ArrayList<>());
        }
        configuration.getConfigurationItems().add(entity);
        configuration = configurationRepository.save(configuration);
        entity = (TEntity) configuration.getConfigurationItems().get(
                configuration.getConfigurationItems().size() - 1);

        return getConverter().convertToDto(entity);
    }

    public TDto update(long configurationId, long id, TDto dto) {
        log.info("update with configurationId: " + configurationId + " and id: " + id);

        Optional<Configuration> configurationOptional = getConfigurationById(configurationId);
        if (!configurationOptional.isPresent()) {
        	log.error("Could not find Configuration for Id: " + configurationId);
        	return null;
        }
        Configuration configuration = configurationOptional.get();
        TEntity entity = getConfigurationItemById(configuration, id);
        getConverter().convertToEntity(dto, entity);
        configuration = configurationRepository.save(configuration);
        entity = getConfigurationItemById(configuration, id);
        return getConverter().convertToDto(entity);
    }

    private Optional<Configuration> getConfigurationById(long configId) {
        return configurationRepository.findById(configId);
    }

    private TEntity getConfigurationItemById(Configuration configuration, long id) {
        AbstractConfigurationItem abstractEntity = configuration.getConfigurationItems()
                .stream()
                .filter(ci -> ci.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (abstractEntity == null) {
            return null;
        }

        try {
            return (TEntity) abstractEntity;
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    String.format("Wrong configuration item type: %s",
                            abstractEntity.getClass().getSimpleName()), ex);
        }
    }
}
