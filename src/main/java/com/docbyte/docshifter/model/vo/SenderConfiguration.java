package com.docbyte.docshifter.model.vo;

import java.io.Serializable;

public class SenderConfiguration implements Serializable
{
	private static final long serialVersionUID = 3244939695537905315L;
	
	private long id;
	private String name;
	private String description;
	private ModuleConfiguration inputConfiguration;
	
	public SenderConfiguration() {}
	
	public SenderConfiguration(String name, String description)
	{
		this.name = name;
		this.description = description;
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

	public void setInputConfiguration(ModuleConfiguration configuration)
	{	
		if(configuration==null)
			throw new IllegalArgumentException("No module configuration specified as inputconfiguration! ");

		
		//check if module configuration is configuration of a sender module
		if(!configuration.getModule().getType().equals("Input"))
			throw new IllegalArgumentException("The moduleconfiguration with id " + configuration.getId() + " is not a configuration for a sender module!");
		
		this.inputConfiguration = configuration;
	}

	public ModuleConfiguration getInputConfiguration()
	{
		return inputConfiguration;
	}

	/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((inputConfiguration == null) ? 0 : inputConfiguration
						.hashCode());
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
		SenderConfiguration other = (SenderConfiguration) obj;
		if (inputConfiguration == null) {
			if (other.inputConfiguration != null)
				return false;
		} else if (!inputConfiguration.equals(other.inputConfiguration))
			return false;
		return true;
	}
	*/
	
	public boolean compareTo(Object obj){
		boolean equals = false;
		
		if(obj instanceof SenderConfiguration){
			SenderConfiguration sc = (SenderConfiguration) obj;
			
			equals = sc.getInputConfiguration().compareTo(this.inputConfiguration);
		}
		
		return equals;
	}
}
