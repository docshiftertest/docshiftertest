package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.entities.DbConfigurationItem;
import com.docshifter.core.monitoring.mappings.ConfigurationItemConverter;
import com.docshifter.core.monitoring.mappings.DbConfigurationItemConverter;
import com.docshifter.core.monitoring.repo.DbConfigurationItemRepository;
import com.docshifter.core.monitoring.services.AbstractConfigurationItemService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DbConfigurationItemService extends AbstractConfigurationItemService<DbConfigurationItem, DbConfigurationItemDto>
{
	private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.DbConfigurationItemService.class.getName());

	@Autowired
    private DbConfigurationItemConverter converter;

    @Autowired
    private DbConfigurationItemRepository repository;

    @Override
    protected ConfigurationItemConverter<DbConfigurationItem, DbConfigurationItemDto> getConverter() {
        return converter;
    }

    public void delete(long configurationId, long id) {
        Optional<DbConfigurationItem> entityOptional = repository.findById(id);
        if (!entityOptional.isPresent()) {
         	log.info("Just FYI, could not find DbConfigurationItem to delete for Id: " + id);
        }
        else {
        	DbConfigurationItem entity = entityOptional.get();
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
