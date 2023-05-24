package com.docshifter.core.config.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.DiffIgnore;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
@DiffIgnore
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
	@ManyToOne
	@Nullable
	private Parameter dependsOn;
	@ManyToOne
	@Nullable
	private Parameter expendableBy;
	@ManyToOne
	@Nullable
	private Parameter aliasOf;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	@JoinTable(name = "moduleparams",
			joinColumns = {	@JoinColumn(name = "param") },
			inverseJoinColumns = { @JoinColumn(name = "module") })
	private Module module;
	
	public Parameter() {}
	
	public Parameter(String name, String description, ParameterTypes type, Boolean required, String valuesJson,
					 String parameterGroup, @Nullable Parameter dependsOn, @Nullable Parameter expendableBy) {
		this.name = name;
		this.description = description;
		this.type = type.toString();
		this.required = required;
		this.valuesJson = valuesJson;
		this.parameterGroup = parameterGroup;
		this.dependsOn = dependsOn;
		this.expendableBy = expendableBy;
	}

	public Parameter(String name, @Nullable Parameter aliasOf) {
		this.name = name;
		this.aliasOf = aliasOf;
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

	public long getId()
	{
		return id;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getType()
	{
		return type;
	}

    public String getValuesJson() {
        return valuesJson;
    }

    public void setValuesJson(String valuesJson) {
        this.valuesJson = valuesJson;
    }
    
	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

    public String getParameterGroup() {
		return parameterGroup;
	}

	public void setParameterGroup(String parameterGroup) {
		this.parameterGroup = parameterGroup;
	}

	@Nullable
	public Parameter getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(@Nullable Parameter dependsOn) {
		this.dependsOn = dependsOn;
	}

	@Nullable
	public Parameter getExpendableBy() {
		return expendableBy;
	}

	public void setExpendableBy(@Nullable Parameter expendableBy) {
		this.expendableBy = expendableBy;
	}

	@Nullable
	public Parameter getAliasOf() {
		return aliasOf;
	}

	public void setAliasOf(@Nullable Parameter aliasOf) {
		this.aliasOf = aliasOf;
		if (aliasOf != null) {
			this.description = null;
			this.type = null;
			this.required = null;
			this.valuesJson = null;
			this.parameterGroup = null;
			this.dependsOn = null;
			this.expendableBy = null;
		}
	}

	public Module getModule() {
		return module;
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
	public String toString() {
		return "{" +
				"\"id\": " + id +
				", \"name\": \"" + name + '\"' +
				", \"description\": \"" + description + '\"' +
				", \"type\": \"" + type + '\"' +
				", \"required\": \"" + required + '\"' +
				", \"valuesJson\": \"" + valuesJson + '\"' +
				", \"parameterGroup\": \"" + parameterGroup + '\"' +
				'}';
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
