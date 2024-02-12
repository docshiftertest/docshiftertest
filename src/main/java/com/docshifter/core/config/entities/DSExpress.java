package com.docshifter.core.config.entities;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.DiffInclude;

import java.io.Serializable;

@Entity
public class DSExpress implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @DiffInclude
    private String name;

    @DiffInclude
    private String description;

    @DiffInclude
    private boolean enabled;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne
    @Nonnull
    @DiffIgnore
    private ChainConfiguration chainConfiguration;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String parameterValues;

    public DSExpress() {}

    public DSExpress(long id,
                     String name,
                     String description,
                     boolean enabled,
                     @Nonnull ChainConfiguration chainConfiguration,
                     String parameterValues) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.chainConfiguration = chainConfiguration;
        this.parameterValues = parameterValues;
    }

    public DSExpress(String name,
                     String description,
                     boolean enabled,
                     @Nonnull ChainConfiguration chainConfiguration,
                     String parameterValues) {
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.chainConfiguration = chainConfiguration;
        this.parameterValues = parameterValues;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @ManyToOne(
            cascade = CascadeType.PERSIST,
            fetch = FetchType.EAGER
    )
    @JoinColumn(name = "chain_configuration_id")
    @Nonnull
    public ChainConfiguration getChainConfiguration() {
        return chainConfiguration;
    }

    public void setChainConfiguration(@Nonnull ChainConfiguration chainConfiguration) {
        this.chainConfiguration = chainConfiguration;
    }

    public String getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(String taskData) {
        this.parameterValues = taskData;
    }
}
