package com.docshifter.core.config.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Parameter
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String name;
	private String description;
	private String type;
	private Boolean required;
	@Column(length = 1000)
	private String valuesJson;
	
	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}
	public Parameter() {}
	
	public Parameter(String name, String description, ParameterTypes type, Boolean required, String valuesJson)
	{
		this.name = name;
		this.description = description;
		this.type = type.toString();
		this.required = required;
		this.valuesJson = valuesJson;
	}

    public Parameter(String name, String description, ParameterTypes type, Boolean required) {
	    this(name, description, type, required, null);
    }
	
	public Parameter(String name, String description, ParameterTypes type) {
		this(name, description, type, false, null);
	}

	public Parameter(String name, ParameterTypes type) {
		this(name, null, type, false, null);
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
				'}';
	}
}
