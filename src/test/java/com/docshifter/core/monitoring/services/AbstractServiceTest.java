package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.AbstractSpringTest;
import com.docshifter.core.monitoring.dtos.ConfigurationDto;
import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.SnmpConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.docshifter.core.monitoring.services.ConfigurationService;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by blazejm on 25.07.2017.
 */
public abstract class AbstractServiceTest extends AbstractSpringTest {
    protected ConfigurationDto sampleConfiguration1;
    protected MailConfigurationItemDto mailConfigurationItem;
    protected WebhookConfigurationItemDto webhookConfigurationItem;
    protected SnmpConfigurationItemDto snmpConfigurationItem;

    @Autowired
    protected ConfigurationService configurationService;

    @Before
    public void beforeTest() {
        mailConfigurationItem = new MailConfigurationItemDto();
        mailConfigurationItem.setToAddresses("test1@email.com, test2@email.com");
        mailConfigurationItem.setHost("localhost");
        mailConfigurationItem.setPassword("secret");
        mailConfigurationItem.setPort(587);
        mailConfigurationItem.setFromAddress("blaze@localhost");
        mailConfigurationItem.setSsl(false);

        mailConfigurationItem.setNotificationLevels(Arrays.asList(
                NotificationLevels.ERROR,
                NotificationLevels.WARN
        ));

        mailConfigurationItem.setTemplateTitle("test title");

        webhookConfigurationItem = new WebhookConfigurationItemDto();
        webhookConfigurationItem.setUrl("http://localhost/webhook");
        webhookConfigurationItem.setNotificationLevels(Arrays.asList(
                NotificationLevels.ERROR,
                NotificationLevels.WARN
        ));

        snmpConfigurationItem = new SnmpConfigurationItemDto();
        snmpConfigurationItem.setNotificationLevels(Arrays.asList(
                NotificationLevels.ERROR,
                NotificationLevels.WARN
        ));

        sampleConfiguration1 = new ConfigurationDto();
        sampleConfiguration1.setId(11L);
        sampleConfiguration1.setName("test");
        sampleConfiguration1.setConfigurationItems(Arrays.asList(
                mailConfigurationItem,
                webhookConfigurationItem,
                snmpConfigurationItem
        ));
    }

    @After
    public void clearDown() {
    	List<ConfigurationDto> configs = configurationService.getAll();
    	configs.forEach(config -> configurationService.deleteConfiguration(config.getId()));
    }

    protected void saveConfiguration() {
        sampleConfiguration1 = configurationService.addConfiguration(sampleConfiguration1);

        sampleConfiguration1.getConfigurationItems()
                .stream()
                .forEach(ci -> {
                    if (ci instanceof MailConfigurationItemDto) {
                        mailConfigurationItem = (MailConfigurationItemDto) ci;
                        assertThat(mailConfigurationItem.getId()).isNotNull();
                    } else if (ci instanceof  WebhookConfigurationItemDto) {
                        webhookConfigurationItem = (WebhookConfigurationItemDto) ci;
                    } else if (ci instanceof  SnmpConfigurationItemDto) {
                        snmpConfigurationItem = (SnmpConfigurationItemDto) ci;
                    }
                });
    }
}
