package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;
import com.docshifter.core.monitoring.utils.TemplateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

/**
 * Created by blazejm on 16.05.2017.
 */
@Service
@Log4j2
public class WebhookNotificationServiceImpl implements WebhookNotificationService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void sendNotification(WebhookConfigurationItemDto webhookConfigItem, NotificationDto notification) {
        try {
            ResponseEntity<String> response;
            if (webhookConfigItem != null) {
                HttpEntity<String> entity = new HttpEntity<>(
                        getTextFromTemplate(webhookConfigItem.getBody(), notification),
                        getHeaders(webhookConfigItem));
    
                String url = getUrl(webhookConfigItem.getUrl(), webhookConfigItem);
                
                log.debug("sending Notification to: " + url + "with entity: " + entity + " ");
                response= restTemplate.postForEntity(url, entity, String.class);
            } else {
                response= restTemplate.postForEntity(webhookConfigItem.getUrl(), notification, String.class);
            }
            
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                log.info("sendNotification to rest API: " + webhookConfigItem.getUrl()
                        + "with message: " + notification.getMessage() + " successful");
            } else {
                log.error("Status code {} returned with body {}", response.getStatusCode(), response.getBody());
            }
            
            
        } catch (HttpClientErrorException ex) {
            log.error("Client exception: {}", ex.getMessage(), ex);
            log.debug(ex.getResponseBodyAsString());
            
            ex.printStackTrace();
        }catch (Exception ex) {
            log.error("Unknown exception: {}", ex.getMessage(), ex);
    
            ex.printStackTrace();
        }
    }

    private HttpHeaders getHeaders(WebhookConfigurationItemDto webhookConfigItem) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        if (webhookConfigItem.getHeaderParams() != null) {
            webhookConfigItem.getHeaderParams()
                    .forEach(hp -> headers.add(hp.getKey(), hp.getValue()));
        }
        return headers;
    }

    private String getUrl(String url, WebhookConfigurationItemDto webhookConfigItem) {
        if (webhookConfigItem.getUrlParams() != null) {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
            webhookConfigItem.getUrlParams()
                    .forEach(up -> uriBuilder.queryParam(up.getKey(), up.getValue()));
            return uriBuilder.toUriString();
        }
        return url;
    }

    private String getTextFromTemplate(String template, NotificationDto notification) {
        return TemplateUtils.getTextFromTemplate(template, notification,null);
    }

}
