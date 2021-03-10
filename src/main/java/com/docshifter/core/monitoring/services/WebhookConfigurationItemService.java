package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;
import com.docshifter.core.monitoring.entities.WebhookConfigurationItem;
import com.docshifter.core.monitoring.entities.WebhookTemplate;
import com.docshifter.core.monitoring.mappings.ConfigurationItemConverter;
import com.docshifter.core.monitoring.mappings.WebhookConfigurationItemConverter;
import com.docshifter.core.monitoring.repositories.WebhookConfigurationItemRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional("dsTransactionManager")
public class WebhookConfigurationItemService extends AbstractConfigurationItemService<WebhookConfigurationItem, WebhookConfigurationItemDto>
{
	private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.WebhookConfigurationItemService.class.getName());

    @Autowired
    private WebhookConfigurationItemConverter converter;

    @Autowired
    private WebhookConfigurationItemRepository repository;

    @Override
    protected ConfigurationItemConverter<WebhookConfigurationItem, WebhookConfigurationItemDto> getConverter() {
        return converter;
    }

    public void delete(long configurationId, long id) {
    	Optional<WebhookConfigurationItem> entityOptional = repository.findById(id);
    	if (!entityOptional.isPresent()) {
         	log.info("Just FYI, could not find WebhookConfigurationItem to delete for Id: " + id);
         	return;
        }
        WebhookConfigurationItem entity = entityOptional.get();
    	if (entity.getConfiguration().getId() == configurationId) {
        	if (entity.getWebhookTemplates() != null) {
        		for (WebhookTemplate template: entity.getWebhookTemplates()) {
        			entityManager.remove(template);
        		}
        	}
        	entityManager.remove(entity);
    	}
        else {
    		log.error("Delete denied! Configuration Id specified: " + configurationId 
    				+ " does not match entity Configuration ID: " + entity.getConfiguration().getId());
    	}
    }
}
