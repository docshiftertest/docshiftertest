package com.docshifter.core.monitoring.dtos;

import com.docshifter.core.monitoring.dtos.AbstractConfigurationItemDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class ConfigurationDto {
    private Long id;
    private String name;

    @JsonIgnoreProperties({"password"})
    private List<AbstractConfigurationItemDto> configurationItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AbstractConfigurationItemDto> getConfigurationItems() {
        return configurationItems;
    }

    public void setConfigurationItems(List<AbstractConfigurationItemDto> configurationItems) {
        this.configurationItems = configurationItems;
    }
}
