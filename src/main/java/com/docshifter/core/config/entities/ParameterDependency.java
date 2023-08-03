package com.docshifter.core.config.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.DiffIgnore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Defines a dependency relation between two {@link Parameter}s.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
@DiffIgnore
@Data
@NoArgsConstructor
@IdClass(ParameterDependency.CompositeId.class)
public class ParameterDependency implements Serializable {
    @Data
    public static class CompositeId implements Serializable {
        private Parameter parameter;
        private Parameter dependee;
    }

    /**
     * The type of dependency.
     */
    public enum Type {
        /**
         * The {@link #parameter} depends on {@link #dependee}. This means that it should not be available unless
         * {@link #dependee} has a value set.
         */
        DEPENDS_ON,
        /**
         * The {@link #parameter} is expendable by {@link #dependee}. This means if {@link #dependee} has a value set,
         * the {@link #parameter} should become unavailable.
         */
        EXPENDABLE_BY
    }

    /**
     * The main subject of the dependency relation, i.e. the parameter whose availability will dynamically change.
     */
    @Id
    @ManyToOne
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Nonnull
    private Parameter parameter;

    /**
     * The other side of the dependency relation, i.e. the parameter that is depended on and of which the value should
     * be monitored.
     */
    @Id
    @ManyToOne
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Nonnull
    private Parameter dependee;

    /**
     * The type of dependency.
     */
    @Enumerated(EnumType.STRING)
    @Nonnull
    private Type type;

    /**
     * When not null, the dependency relation will only trigger in case {@link #dependee} has this specific parameter
     * value. Multiple possible values can be specified by separating them by || (similar to option module branch values).
     */
    @Nullable
    private String havingValue;

    public ParameterDependency(@Nonnull Parameter parameter, @Nonnull Parameter dependee, @Nonnull Type type) {
        this(parameter, dependee, type, null);
    }

    public ParameterDependency(@Nonnull Parameter parameter, @Nonnull Parameter dependee, @Nonnull Type type, @Nullable String havingValue) {
        checkDependencyRelation(parameter, dependee);
        this.parameter = parameter;
        this.dependee = dependee;
        this.type = type;
        this.havingValue = havingValue;
    }

    @Nonnull
    @JsonIgnore
    @EqualsAndHashCode.Include
    public Parameter getParameter() {
        return parameter.getRealParameter();
    }

    public void setParameter(@Nonnull Parameter parameter) {
        checkDependencyRelation(dependee, parameter);
        this.parameter = parameter;
    }

    @Nonnull
    @JsonIgnore
    public Parameter getRawParameter() {
        return parameter;
    }

    @Nonnull
    @JsonIgnore
    @EqualsAndHashCode.Include
    public Parameter getDependee() {
        return dependee.getRealParameter();
    }

    @Nonnull
    @JsonIgnore
    public Parameter getRawDependee() {
        return dependee;
    }

    @Nonnull
    @JsonProperty("dependee")
    public Long getDependeeId() {
        return getDependee().getRawId();
    }

    public void setDependee(@Nonnull Parameter dependee) {
        checkDependencyRelation(parameter, dependee);
        this.dependee = dependee;
    }

    private void checkDependencyRelation(@Nonnull Parameter existingRelation, @Nonnull Parameter newRelation) {
        if (existingRelation.getRealParameter().equals(newRelation.getRealParameter())) {
            throw new IllegalArgumentException("The dependency cannot point to itself (" + existingRelation.getName() + ")!");
        }
    }

    /**
     * Gets the exact opposite of this dependency relation, i.e. the {@link #parameter} and {@link #dependee} will
     * switch places.
     */
    @Nonnull
    @JsonIgnore
    public ParameterDependency opposite() {
        return new ParameterDependency(dependee, parameter, type, havingValue);
    }
}
