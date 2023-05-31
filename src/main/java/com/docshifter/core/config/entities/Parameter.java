package com.docshifter.core.config.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.DiffIgnore;

import javax.annotation.Nullable;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Optional;

/**
 * A parameter is a configurable property within a {@link Module}. The entity that stores all parameters for a given
 * {@link Module} is called a {@link ModuleConfiguration}.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
@DiffIgnore
@ToString
public class Parameter implements Comparable<Parameter>, Serializable
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String name;
	private String description;
	private String type;
	private Boolean required;

	@Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
	@Column(columnDefinition = "jsonb")
	private String valuesJson;
	private String parameterGroup;
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	@Nullable
	@ToString.Exclude
	private Parameter dependsOn;
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	@Nullable
	@ToString.Exclude
	private Parameter expendableBy;
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	@Nullable
	private Parameter aliasOf;
	@Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
	@Column(columnDefinition = "jsonb")
	@Nullable
	private String aliasMappings;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	@JoinTable(name = "moduleparams",
			joinColumns = {	@JoinColumn(name = "param") },
			inverseJoinColumns = { @JoinColumn(name = "module") })
	private Module module;
	
	public Parameter() {}

	/**
	 * Constructs a "normal" {@link Parameter}.
	 * @param name The name of the {@link Parameter}.
	 * @param description A description that explains how the {@link Parameter} behaves, how it can be configured,
	 *                       important things to look out for etc.
	 * @param type The type of {@link Parameter}, see {@link ParameterTypes}.
	 * @param required Whether the {@link Parameter} must be filled in for the {@link ModuleConfiguration} to be functional.
	 * @param valuesJson When the {@link Parameter} value can only be one out of a specific list of options, or if some
	 *                      potential values should be suggested to the user, this JSON can be used to specify all of
	 *                      the different options.
	 * @param parameterGroup The name of the group this {@link Parameter} should be a part of.
	 * @param dependsOn Another {@link Parameter} that the current one should depend on, if applicable. This means that
	 *                     if the other {@link Parameter} has a value set, the current one should become required.
	 * @param expendableBy Another {@link Parameter} that the current one should be expendable by, if applicable. This
	 *                        means that if the other {@link Parameter} has a value set, the current one should become
	 *                        not available.
	 */
	public Parameter(String name, String description, ParameterTypes type, Boolean required, String valuesJson,
					 String parameterGroup, @Nullable Parameter dependsOn, @Nullable Parameter expendableBy) {
		this.name = name;
		this.description = description;
		this.type = type.toString();
		this.required = required;
		this.valuesJson = valuesJson;
		this.parameterGroup = parameterGroup;
		if (dependsOn != null && dependsOn.equals(expendableBy)) {
			throw new IllegalArgumentException("Conflicting dependency detected: dependsOn cannot be the same as expendableBy (" + dependsOn.name + ")!");
		}
		this.dependsOn = dependsOn;
		this.expendableBy = expendableBy;
	}

	/**
	 * Constructs an "alias" {@link Parameter}. This type of {@link Parameter} simply points to another
	 * {@link Parameter}. It is not meant to be configured by its own, but could be a leftover due to historical reasons.
	 * @param name The name of the {@link Parameter}.
	 * @param aliasOf The actual {@link Parameter} to point to.
	 * @param aliasMappings When migrating to the actual {@link Parameter}, this may contain a JSON that details how
	 *                         values should be mapped to this new {@link Parameter}.
	 */
	public Parameter(String name, @Nullable Parameter aliasOf, @Nullable String aliasMappings) {
		this.name = name;
		this.aliasOf = aliasOf;
		this.aliasMappings = aliasMappings;
	}

	public Parameter(String name, String description, ParameterTypes type, Boolean required, String valuesJson,
					 String parameterGroup) {
		this(name, description, type, required, valuesJson, parameterGroup, null, null);
	}

    public Parameter(String name, String description, ParameterTypes type, Boolean required) {
	    this(name, description, type, required, null, null, null, null);
    }
	
	public Parameter(String name, String description, ParameterTypes type) {
		this(name, description, type, false, null, null, null, null);
	}

	public Parameter(String name, ParameterTypes type) {
		this(name, null, type, false, null, null, null, null);
	}

	public void setId(long id)
	{
		this.id = id;
	}

	/**
	 * Gets the ID of this {@link Parameter}, or the actual {@link Parameter} it is pointing to if the current one is an
	 * alias.
	 */
	public long getId() {
		return getRealParameter().id;
	}

	/**
	 * Gets the ID of this {@link Parameter}.
	 */
	public long getRawId() {
		return id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the name of this {@link Parameter}, or the actual {@link Parameter} it is pointing to if the current one is an
	 * alias.
	 */
	public String getName()
	{
		return getRealParameter().name;
	}

	/**
	 * Gets the name of this {@link Parameter}.
	 */
	public String getRawName() {
		return name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the description of this {@link Parameter}, or the actual {@link Parameter} it is pointing to if the current one is an
	 * alias.
	 */
	public String getDescription() {
		return getRealParameter().description;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * Gets the type of this {@link Parameter} in {@link String} format, or the actual {@link Parameter} it is pointing
	 * to if the current one is an alias.
	 * @see ParameterTypes
	 */
	public String getType() {
		return getRealParameter().type;
	}

	/**
	 * Gets the values JSON of this {@link Parameter}, or the actual {@link Parameter} it is pointing to if the current one is an
	 * alias.
	 */
    public String getValuesJson() {
        return getRealParameter().valuesJson;
    }

    public void setValuesJson(String valuesJson) {
        this.valuesJson = valuesJson;
    }

	/**
	 * Gets the required flag of this {@link Parameter}, or the actual {@link Parameter} it is pointing to if the current one is an
	 * alias.
	 */
	public Boolean getRequired() {
		return getRealParameter().required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	/**
	 * Gets the group of this {@link Parameter}, or the actual {@link Parameter} it is pointing to if the current one is an
	 * alias.
	 */
    public String getParameterGroup() {
		return getRealParameter().parameterGroup;
	}

	public void setParameterGroup(String parameterGroup) {
		this.parameterGroup = parameterGroup;
	}

	/**
	 * Gets the {@link Parameter} that the current {@link Parameter} (or the actual {@link Parameter} it is pointing to if
	 * the current one is an alias) depends on, if applicable. This means that if the other {@link Parameter} has a
	 * value set, the current one should become required.
	 */
	@Nullable
	@JsonIgnore
	public Parameter getDependsOn() {
		return getRealParameter().dependsOn;
	}

	@Nullable
	@JsonProperty("dependsOn")
	public Long getDependsOnId() {
		return Optional.ofNullable(getDependsOn()).map(Parameter::getRawId).orElse(null);
	}

	/**
	 * Sets the {@link Parameter} that the current {@link Parameter} depends on. This means that if the other
	 * {@link Parameter} has a value set, the current one should become required. Use {@code null} if it should not
	 * depend on anything else.
	 */
	public void setDependsOn(@Nullable Parameter dependsOn) {
		if (dependsOn != null) {
			if (equals(dependsOn)) {
				throw new IllegalArgumentException("The dependsOn cannot point to itself (" + name + ")!");
			}
			if (equals(dependsOn.getDependsOn())) {
				throw new IllegalArgumentException("Circular dependsOn dependency detected: the targeted parameter "
						+ dependsOn.name + " is already pointing to this one (" + name + ")!");
			}
			if (dependsOn.equals(expendableBy)) {
				throw new IllegalArgumentException("Conflicting dependency detected: dependsOn cannot be the same as expendableBy (" + dependsOn.name + ")!");
			}
		}
		this.dependsOn = dependsOn;
	}

	/**
	 * Gets the {@link Parameter} that the current {@link Parameter} (or the actual {@link Parameter} it is pointing to if
	 * the current one is an alias) is expendable by, if applicable. This means that if the other {@link Parameter} has a
	 * value set, the current one should become not available.
	 */
	@Nullable
	@JsonIgnore
	public Parameter getExpendableBy() {
		return getRealParameter().expendableBy;
	}

	@Nullable
	@JsonProperty("expendableBy")
	public Long getExpendableById() {
		return Optional.ofNullable(getExpendableBy()).map(Parameter::getRawId).orElse(null);
	}

	/**
	 * Sets the {@link Parameter} that the current {@link Parameter} is expendable by. This means that if the other
	 * {@link Parameter} has a value set, the current one should become not available. Use {@code null} if it should not
	 * be expendable by anything else.
	 */
	public void setExpendableBy(@Nullable Parameter expendableBy) {
		if (expendableBy != null) {
			if (equals(expendableBy)) {
				throw new IllegalArgumentException("The expendableBy cannot point to itself (" + name + ")!");
			}
			if (equals(expendableBy.getExpendableBy())) {
				throw new IllegalArgumentException("Circular expendableBy dependency detected: the targeted parameter "
						+ expendableBy.name + " is already pointing to this one (" + name + ")!");
			}
			if (expendableBy.equals(dependsOn)) {
				throw new IllegalArgumentException("Conflicting dependency detected: dependsOn cannot be the same as expendableBy (" + dependsOn.name + ")!");
			}
		}
		this.expendableBy = expendableBy;
	}

	/**
	 * Gets the actual {@link Parameter} that this one is aliasing, if applicable (otherwise {@code null}). An alias
	 * simply points to another {@link Parameter}. It is not meant to be configured by its own, but could be a leftover
	 * due to historical reasons.
	 */
	@Nullable
	@JsonIgnore
	public Parameter getAliasOf() {
		return aliasOf;
	}

	/**
	 * Sets the actual {@link Parameter} that this one is aliasing, or {@code null} if it is not an alias. An alias
	 * simply points to another {@link Parameter}. It is not meant to be configured by its own, but could be a leftover
	 * due to historical reasons.
	 */
	public void setAliasOf(@Nullable Parameter aliasOf) {
		if (aliasOf != null) {
			if (equals(aliasOf)) {
				throw new IllegalArgumentException("The aliasOf cannot point to itself (" + name + ")!");
			}
			if (equals(aliasOf.getRealParameter())) {
				throw new IllegalArgumentException("Circular aliasOf dependency detected: the targeted parameter "
						+ aliasOf.name + " is already pointing to this one (" + name + ")!");
			}
			this.description = null;
			this.type = null;
			this.required = null;
			this.valuesJson = null;
			this.parameterGroup = null;
			this.dependsOn = null;
			this.expendableBy = null;
		}
		this.aliasOf = aliasOf;
	}

	/**
	 * Gets the alias mappings JSON. When migrating to the actual {@link Parameter}, this may contain a JSON that details how
	 * values should be mapped to this new {@link Parameter}.
	 */
	@Nullable
	public String getAliasMappings() {
		return aliasMappings;
	}

	/**
	 * Sets the alias mappings JSON. When migrating to the actual {@link Parameter}, this may contain a JSON that details how
	 * values should be mapped to this new {@link Parameter}.
	 */
	public void setAliasMappings(@Nullable String aliasMappings) {
		this.aliasMappings = aliasMappings;
	}

	/**
	 * Gets the actual {@link Parameter}. This is the current {@link Parameter} if it is not an alias, or the
	 * {@link Parameter} that is being pointed to if it is.
	 */
	@JsonIgnore
	public Parameter getRealParameter() {
		if (aliasOf == null) {
			return this;
		}
		return aliasOf.getRealParameter();
	}

	/**
	 * Gets the {@link Module} this {@link Parameter} is for.
	 */
	@JsonIgnore
	public Module getModule() {
		return module;
	}

	/**
	 * Sets the {@link Module} this {@link Parameter} is for.
	 */
	public void setModule(Module module) {
		this.module = module;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public int compareTo(Parameter o) {
		if (this.getName() != null && o.getName() != null) {
			return this.getName().compareTo(o.getName());
		}
		else if (this.getDescription() != null && o.getDescription() != null) {
			return this.getDescription().compareTo(o.getDescription());
		}
		else {
			return Long.compare(this.getId(), o.getId());
		}
	}
}
