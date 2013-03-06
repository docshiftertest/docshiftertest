package com.docbyte.docshifter.model.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ReceiverConfiguration implements Serializable
{
	private static final long serialVersionUID = -3514478844168465767L;
	
	private long id;
	private String name;
	private String description;
	private ModuleConfiguration transformationConfiguration;
	
	private Set<ModuleConfiguration> releaseConfiguration = new HashSet<ModuleConfiguration>();
	
	public ReceiverConfiguration() {}
	
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

	public void setTransformationConfiguration(ModuleConfiguration transformationConfiguration)
	{
		this.transformationConfiguration = transformationConfiguration;
	}

	public ModuleConfiguration getTransformationConfiguration()
	{
		return transformationConfiguration;
	}

	public void setReleaseConfiguration(Set<ModuleConfiguration> releaseConfiguration)
	{
		this.releaseConfiguration = releaseConfiguration;
	}

	public Set<ModuleConfiguration> getReleaseConfiguration()
	{
		return releaseConfiguration;
	}
}
