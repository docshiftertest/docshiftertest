package com.docbyte.docshifter.model.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ReceiverConfiguration implements Serializable
{
	

	private static final long serialVersionUID = -3514478844168465767L;
	
	private long id;
	private String name;
	private String description;
	private Map<Integer,ModuleConfiguration> transformationConfiguration = new HashMap<Integer,ModuleConfiguration>();
	
	private Set<ModuleConfiguration> releaseConfiguration = new HashSet<ModuleConfiguration>();
	
	public ReceiverConfiguration(long id, String name, String description,
			Map<Integer, ModuleConfiguration> transformationConfiguration,
			Set<ModuleConfiguration> releaseConfiguration) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		setTransformationConfiguration(transformationConfiguration);
		setReleaseConfiguration(releaseConfiguration);
	}
	
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

	public void setTransformationConfiguration(Map<Integer,ModuleConfiguration> transformationConfiguration)
	{
		//check if configuration is null
		if(transformationConfiguration == null)
			throw new IllegalArgumentException("No transformation configurations specified for receiver configuration! ");
		
		for(ModuleConfiguration moduleconfig:transformationConfiguration.values())
		{
			if(moduleconfig == null)
				throw new IllegalArgumentException("One of the transformationconfigurations is not valid! ");		
		
			//check if configuration is part of transformation module
			if(!moduleconfig.getModule().getType().equals("Transformation"))
				throw new IllegalArgumentException("The moduleconfiguration with id " + moduleconfig.getId() + " is not a configuration for a transformation module!");
		
		}
		this.transformationConfiguration = transformationConfiguration;
	}
	
	public Map<Integer,ModuleConfiguration> getTransformationConfiguration()
	{
		return transformationConfiguration;
	}

	public void setReleaseConfiguration(Set<ModuleConfiguration> releaseConfiguration)
	{
		//check if configuration is null
		if(releaseConfiguration ==null)
			throw new IllegalArgumentException("No releaseconfiguration specified for receiver configuration! ");
		
		for(ModuleConfiguration moduleconfig:releaseConfiguration)
		{
			if(moduleconfig ==null)
				throw new IllegalArgumentException("One of the releaseconfigurations is not valid! ");
				
			//check if configuration is part of release module
			if(!moduleconfig.getModule().getType().equals("Release"))
				throw new IllegalArgumentException("The moduleconfiguration with id " + moduleconfig.getId() + " is not a configuration for a release module!");
		}
					
		
		this.releaseConfiguration = releaseConfiguration;
	}

	public Set<ModuleConfiguration> getReleaseConfiguration()
	{
		return releaseConfiguration;
	}

	/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((releaseConfiguration == null) ? 0 : releaseConfiguration
						.hashCode());
		result = prime
				* result
				+ ((transformationConfiguration == null) ? 0
						: transformationConfiguration.hashCode());
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
		ReceiverConfiguration other = (ReceiverConfiguration) obj;
		if (releaseConfiguration == null) {
			if (other.releaseConfiguration != null)
				return false;
		} else if (!releaseConfiguration.equals(other.releaseConfiguration))
			return false;
		if (transformationConfiguration == null) {
			if (other.transformationConfiguration != null)
				return false;
		} else if (!transformationConfiguration
				.equals(other.transformationConfiguration))
			return false;
		return true;
	}
	*/
	
	public boolean compareTo(Object obj){
		boolean equals = true;
		
		if(obj instanceof ReceiverConfiguration){
			if(obj instanceof ReceiverConfiguration){
				ReceiverConfiguration rc = (ReceiverConfiguration) obj;
				
				if(this.releaseConfiguration.size() != rc.getReleaseConfiguration().size()){
					equals = false;
				}
				else{
					for(ModuleConfiguration mc : this.releaseConfiguration){
						boolean found = false;
						
						Iterator<ModuleConfiguration> it = rc.getReleaseConfiguration().iterator();
						while(it.hasNext() && !found){
							if(mc.compareTo(it.next())){
								found = true;
							}
						}
						
						if(!found){
							equals = false;
						}
					}
				}
				for(ModuleConfiguration mc : transformationConfiguration.values()){
					boolean found = false;
					
					Iterator<Map.Entry<Integer,ModuleConfiguration>> it = rc.getTransformationConfiguration().entrySet().iterator();
					while(it.hasNext() && !found){
						if(mc.compareTo(it.next())){
							found = true;
						}
					}
					
					if(!found){
						equals = false;
					}
				}
			}
		}
		else{
			equals = false;
		}

		return equals;
	}
}
