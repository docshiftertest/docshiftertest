package com.docshifter.core.monitoring.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity(name = "MonitoringConfiguration")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
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
