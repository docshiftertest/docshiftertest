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
		this.inputConfiguration = configuration;
	}

	public ModuleConfiguration getInputConfiguration()
	{
		return inputConfiguration;
	}
}
