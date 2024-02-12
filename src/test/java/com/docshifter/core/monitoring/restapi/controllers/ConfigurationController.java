package com.docshifter.core.monitoring.restapi.controllers;

import com.docshifter.core.monitoring.dtos.ConfigurationDto;
import com.docshifter.core.monitoring.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by blazejm on 11.05.2017.
 */
@RestController
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @GetMapping(path = "/configurations/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ConfigurationDto getById(
            @PathVariable long id
    ) {
        return configurationService.getById(id);
    }

    @GetMapping(path = "/configurations",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConfigurationDto> getAll(
    ) {
        return configurationService.getAll();
    }

    @PostMapping(path = "/configurations",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ConfigurationDto addConfiguration(
            @RequestBody ConfigurationDto body
    ) {
        return configurationService.addConfiguration(body);
    }

    @PutMapping(path = "/configurations/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ConfigurationDto updateConfiguration(
            @PathVariable long id,
            @RequestBody ConfigurationDto body
    ) {
        return configurationService.updateConfiguration(id, body);
    }

    @DeleteMapping(path = "/configurations/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteConfiguration(
            @PathVariable long id
    ) {
        configurationService.deleteConfiguration(id);
    }

}
