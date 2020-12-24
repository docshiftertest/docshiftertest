package com.docshifter.core.monitoring.entities;

import com.docshifter.core.monitoring.entities.AbstractConfigurationItem;

import javax.persistence.*;
import java.util.List;

@Entity(name = "MonitoringConfiguration")
public class Configuration {
    @Id
    private Long id;
    private String name;

    @OneToMany(mappedBy="configuration",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<AbstractConfigurationItem> configurationItems;

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

    public List<AbstractConfigurationItem> getConfigurationItems() {
        return configurationItems;
    }

    public void setConfigurationItems(List<AbstractConfigurationItem> configurationItems) {
        this.configurationItems = configurationItems;
    }
}
