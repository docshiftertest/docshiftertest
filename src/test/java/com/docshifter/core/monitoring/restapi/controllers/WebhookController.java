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

    @RequestMapping(path = "/configurations/{configurationId}/webhooks/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.GET)
    public WebhookConfigurationItemDto getById(
            @PathVariable("configurationId") long configurationId,
            @PathVariable("id") long id
    ) {
        return webhookService.getById(configurationId, id);
    }

    @RequestMapping(path = "/configurations/{configurationId}/webhooks",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST)
    public WebhookConfigurationItemDto addWebhook(
            @PathVariable("configurationId") long configurationId,
            @RequestBody WebhookConfigurationItemDto body
    ) {
        return webhookService.add(configurationId, body);
    }

    @RequestMapping(path = "/configurations/{configurationId}/webhooks/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.PUT)
    public WebhookConfigurationItemDto updateWebhook(
            @PathVariable("configurationId") long configurationId,
            @PathVariable("id") long id,
            @RequestBody WebhookConfigurationItemDto body
    ) {
        return webhookService.update(configurationId, id, body);
    }

    @RequestMapping(path = "/configurations/{configurationId}/webhooks/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.DELETE)
    public void deleteWebhook(
            @PathVariable("configurationId") long configurationId,
            @PathVariable("id") long id
    ) {
        //TODO implent that
        //webhookService.delete(configurationId, id);

    }

}
