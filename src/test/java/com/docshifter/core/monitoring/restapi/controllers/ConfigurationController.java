package com.docshifter.core.monitoring.restapi.controllers;

import com.docshifter.core.monitoring.dtos.ConfigurationDto;
import com.docshifter.core.monitoring.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by blazejm on 11.05.2017.
 */
@RestController
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @RequestMapping(path = "/configurations/{id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.GET)
    public ConfigurationDto getById(
            @PathVariable("id") long id
    ) {
        return configurationService.getById(id);
    }

    @RequestMapping(path = "/configurations",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.GET)
    public List<ConfigurationDto> getAll(
    ) {
        return configurationService.getAll();
    }

    @RequestMapping(path = "/configurations",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.POST)
    public ConfigurationDto addConfiguration(
            @RequestBody ConfigurationDto body
    ) {
        return configurationService.addConfiguration(body);
    }

    @RequestMapping(path = "/configurations/{id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.PUT)
    public ConfigurationDto updateConfiguration(
            @PathVariable("id") long id,
            @RequestBody ConfigurationDto body
    ) {
        return configurationService.updateConfiguration(id, body);
    }

    @RequestMapping(path = "/configurations/{id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            method = RequestMethod.DELETE)
    public void deleteConfiguration(
            @PathVariable("id") long id
    ) {
        configurationService.deleteConfiguration(id);
    }

}
