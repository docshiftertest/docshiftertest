package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MailConfigurationItemServiceTest extends AbstractServiceTest {

    @Autowired
    MailConfigurationItemService configurationItemService;

    @Test
    public void shouldInjectService() {
        assertThat(configurationItemService).isNotNull();
    }

    @Test
    public void shouldSaveConfiguration() {
        MailConfigurationItemDto item = new MailConfigurationItemDto();
        item.setToAddresses("unit@test.com");
        item.setHost(mailConfigurationItem.getHost());
        item.setPort(mailConfigurationItem.getPort());
        item.setUsername(mailConfigurationItem.getUsername());
        item.setPassword(mailConfigurationItem.getPassword());
        item.setFromAddress(mailConfigurationItem.getFromAddress());
        item.setSsl(mailConfigurationItem.isSsl());
        item.setTemplateTitle(mailConfigurationItem.getTemplateTitle());
        MailConfigurationItemDto result = configurationItemService.add(sampleConfiguration1.getId(), item);
        assertNotNull(result);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTemplateTitle()).isNotNull();
        assertThat(result.getHost()).isNotNull();
    }


    @Test
    public void shouldFetchConfigurationById() {
    	saveConfiguration();
    	assertThat(sampleConfiguration1).isNotNull();
    	assertThat(sampleConfiguration1.getId()).isNotNull();
    	assertThat(mailConfigurationItem).isNotNull();
    	assertThat(mailConfigurationItem.getId()).isNotNull();
    	assertThat(configurationItemService).isNotNull();
    	MailConfigurationItemDto resultById = configurationItemService.getById(sampleConfiguration1.getId(), mailConfigurationItem.getId());
    	assertNotNull(resultById);
        assertThat(resultById.getId()).isEqualTo(mailConfigurationItem.getId());
        assertThat(resultById.getHost()).isEqualTo(mailConfigurationItem.getHost());
        assertThat(resultById.getPort()).isEqualTo(mailConfigurationItem.getPort());
        assertThat(resultById.getTemplateTitle()).isEqualTo(mailConfigurationItem.getTemplateTitle());
    }

    @Test
    public void shouldUpdateConfiguration() {
    	saveConfiguration();
    	assertThat(sampleConfiguration1).isNotNull();
    	assertThat(sampleConfiguration1.getId()).isNotNull();
    	assertThat(mailConfigurationItem).isNotNull();
    	assertThat(mailConfigurationItem.getId()).isNotNull();
    	assertThat(configurationItemService).isNotNull();
    	mailConfigurationItem.setToAddresses("newMail1@test.com   newMail2@test.com;newMail3@test.com , newMail4@test.com");
        MailConfigurationItemDto result = configurationItemService.update(sampleConfiguration1.getId(), mailConfigurationItem.getId(), mailConfigurationItem);
        assertNotNull(result);
        assertThat(result.getToAddresses()).isEqualTo("newMail1@test.com, newMail2@test.com, newMail3@test.com, newMail4@test.com");
    }

    @Test
    public void shouldDeleteConfiguration() {
        saveConfiguration();
        configurationItemService.delete(sampleConfiguration1.getId(), mailConfigurationItem.getId());
        MailConfigurationItemDto resultById = configurationItemService.getById(sampleConfiguration1.getId(), mailConfigurationItem.getId());
        assertNull(resultById);
    }


}
