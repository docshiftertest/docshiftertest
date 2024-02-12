package com.docshifter.core.monitoring.restapi.controllers;

import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;
import com.docshifter.core.monitoring.services.WebhookConfigurationItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Created by blazejm on 16.05.2017.
 */
@RestController
public class WebhookController {

    @Autowired
    private WebhookConfigurationItemService webhookService;

    @GetMapping(path = "/configurations/{configurationId}/webhooks/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebhookConfigurationItemDto getById(
            @PathVariable long configurationId,
            @PathVariable long id
    ) {
        return webhookService.getById(configurationId, id);
    }

    @PostMapping(path = "/configurations/{configurationId}/webhooks",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public WebhookConfigurationItemDto addWebhook(
            @PathVariable long configurationId,
            @RequestBody WebhookConfigurationItemDto body
    ) {
        return webhookService.add(configurationId, body);
    }

    @PutMapping(path = "/configurations/{configurationId}/webhooks/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public WebhookConfigurationItemDto updateWebhook(
            @PathVariable long configurationId,
            @PathVariable long id,
            @RequestBody WebhookConfigurationItemDto body
    ) {
        return webhookService.update(configurationId, id, body);
    }

    @DeleteMapping(path = "/configurations/{configurationId}/webhooks/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteWebhook(
            @PathVariable long configurationId,
            @PathVariable long id
    ) {
        //TODO implent that
        //webhookService.delete(configurationId, id);

    }

}
