package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.SnmpConfigurationItemDto;
import com.docshifter.core.monitoring.entities.SnmpConfigurationItem;
import com.docshifter.core.monitoring.mappings.ConfigurationItemConverter;
import com.docshifter.core.monitoring.mappings.SnmpConfigurationItemConverter;
import com.docshifter.core.monitoring.repo.SnmpConfigurationItemRepository;
import com.docshifter.core.monitoring.services.AbstractConfigurationItemService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class SnmpConfigurationItemService extends AbstractConfigurationItemService<SnmpConfigurationItem, SnmpConfigurationItemDto>
{
	private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.SnmpConfigurationItemService.class.getName());

    @Autowired
    private SnmpConfigurationItemConverter converter;

    @Autowired
    private SnmpConfigurationItemRepository repository;

    @Override
    protected ConfigurationItemConverter<SnmpConfigurationItem, SnmpConfigurationItemDto> getConverter() {
        return converter;
    }

    public void delete(long configurationId, long id) {
        Optional<SnmpConfigurationItem> entityOptional = repository.findById(id);
        if (!entityOptional.isPresent()) {
         	log.info("Just FYI, could not find SnmpConfigurationItem to delete for Id: " + id);
        }
        else {
        	SnmpConfigurationItem entity = entityOptional.get();
        	if (entity.getConfiguration().getId() == configurationId) {
            	entityManager.remove(entity);
        	}
        	else {
        		log.error("Delete denied! Configuration Id specified: " + configurationId 
        				+ " does not match entity Configuration ID: " + entity.getConfiguration().getId());
        	}
        }
    }
}
