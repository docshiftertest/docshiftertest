package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.AbstractSpringTest;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.docshifter.core.monitoring.services.WebhookNotificationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;


/**
 * Created by blazejm on 29.05.2017.
 */
public class WebhookNotificationServiceTest extends AbstractSpringTest {
    @Autowired
    private WebhookNotificationService webhookNotificationService;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private String url = "http://localhost/webhook";

    private WebhookConfigurationItemDto webhookConfigItem;

    @Before
    public void beforeTest() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(requestTo(url))
                .andRespond(MockRestResponseCreators.withSuccess("{ \"result\" : \"OK\"}", MediaType.APPLICATION_JSON_UTF8));

        webhookConfigItem = new WebhookConfigurationItemDto();
        webhookConfigItem.setUrl(url);
    }

    @Test
    public void shouldInjectService() {
        assertThat(webhookNotificationService).isNotNull();
    }

    @Test
    public void shouldSendSimpleNotification() throws InterruptedException {
        NotificationDto notification = new NotificationDto();
        notification.setLevel(NotificationLevels.ERROR);
        notification.setMessage("some body");

        webhookNotificationService.sendNotification(webhookConfigItem, notification);

        mockServer.verify();
    }

    @Test
    public void shouldSendNotificationWithTemplate() throws InterruptedException {
        NotificationDto notification = new NotificationDto();
        notification.setLevel(NotificationLevels.ERROR);
        notification.setMessage("some body");

        webhookConfigItem.setBody("Body: {{message}}, level: {{level}}");

        webhookNotificationService.sendNotification(webhookConfigItem, notification);

        mockServer.verify();
    }

}
