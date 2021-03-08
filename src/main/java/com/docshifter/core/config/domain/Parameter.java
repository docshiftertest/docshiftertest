package com.docshifter.core.config.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
@Table(schema = "DOCSHIFTER", name="PARAMETER")
public class Parameter implements Comparable<Parameter>
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String name;
	private String description;
	private String type;
	private Boolean required;
	@Column(length = 100000)
	private String valuesJson;
	private String parameterGroup;
	
	public Parameter() {}
	
	public Parameter(String name, String description, ParameterTypes type, Boolean required, String valuesJson,String parameterGroup)
	{
		this.name = name;
		this.description = description;
		this.type = type.toString();
		this.required = required;
		this.valuesJson = valuesJson;
		this.parameterGroup = parameterGroup;
	}

    public Parameter(String name, String description, ParameterTypes type, Boolean required) {
	    this(name, description, type, required, null,null);
    }
	
	public Parameter(String name, String description, ParameterTypes type) {
		this(name, description, type, false, null,null);
	}

	public Parameter(String name, ParameterTypes type) {
		this(name, null, type, false, null,null);
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
