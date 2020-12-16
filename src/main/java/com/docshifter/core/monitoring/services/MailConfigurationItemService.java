package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.entities.MailConfigurationItem;
import com.docshifter.core.monitoring.entities.MailTemplate;
import com.docshifter.core.monitoring.mappings.ConfigurationItemConverter;
import com.docshifter.core.monitoring.mappings.MailConfigurationItemConverter;
import com.docshifter.core.monitoring.repo.MailConfigurationItemRepository;
import com.docshifter.core.monitoring.services.AbstractConfigurationItemService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class MailConfigurationItemService extends AbstractConfigurationItemService<MailConfigurationItem, MailConfigurationItemDto>
{
	private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.MailConfigurationItemService.class.getName());

    @Autowired
    private MailConfigurationItemConverter converter;

    @Autowired
    private MailConfigurationItemRepository repository;

    @Override
    protected ConfigurationItemConverter<MailConfigurationItem, MailConfigurationItemDto> getConverter() {
        return converter;
    }

    public void delete(long configurationId, long id) {
        Optional<MailConfigurationItem> entityOptional = repository.findById(id);
        if (!entityOptional.isPresent()) {
        	log.info("Just FYI, could not find MailConfigurationItem to delete for Id: " + id);
        }
        else {
        	MailConfigurationItem entity = entityOptional.get();
        	if (entity.getConfiguration().getId() == configurationId) {
            	if (entity.getMailTemplates() != null) {
            		for (MailTemplate template: entity.getMailTemplates()) {
            			entityManager.remove(template);
            		}
            	}
            	if (entity.getSmtpConfiguration() != null) {
            		entityManager.remove(entity.getSmtpConfiguration());
            	}
            	entityManager.remove(entity);
        	}
        	else {
        		log.error("Delete denied! Configuration Id specified: " + configurationId 
        				+ " does not match entity Configuration ID: " + entity.getConfiguration().getId());
        	}
        }
    }
}
