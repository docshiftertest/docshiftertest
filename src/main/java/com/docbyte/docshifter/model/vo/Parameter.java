package com.docbyte.docshifter.model.vo;

import java.io.Serializable;

import com.docbyte.docshifter.util.ParameterTypes;

public class Parameter implements Serializable
{
	private static final long serialVersionUID = -4973841817482543428L;
	
	private long id;
	private String name;
	private String description;
	private String type;
	private Boolean required;
	
	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}
	public Parameter() {}
	
	public Parameter(String name, String description, ParameterTypes type, Boolean required) 
	{
		this.name = name;
		this.description = description;
		this.type = type.toString();
		this.required = required;
	}
	
	public Parameter(String name, String description, ParameterTypes type) {
		this(name, description, type, false);
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
}
