package com.docbyte.docshifter.model.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ModuleConfiguration implements Serializable {

	private static final long serialVersionUID = 7666888692582226276L;
	
	private long id;
	private Module module;
	private String name;
	private String description;
	private Map<Parameter, String> parameterValues = new HashMap<Parameter, String>();
	
	public ModuleConfiguration() {}
	
	public void setId(long id)
	{
		this.id = id;
	}

	public long getId()
	{
		return id;
	}

	public void setModule(Module module)
	{
		this.module = module;
	}

	public Module getModule()
	{
		return module;
	}

	public void setParameterValues(Map<Parameter, String> parameterValues)
	{
		this.parameterValues = parameterValues;
	}
	
	public void setParameterValue(Parameter param, String value)
	{
		for(Parameter existingParam : this.getParameterValues().keySet())
		{
			if(existingParam.getId() == param.getId())
				this.getParameterValues().put(existingParam, value);
		}
	}

	public Map<Parameter, String> getParameterValues()
	{
		return parameterValues;
	}
	
	public void addParameterValue(Parameter param, String value)
	{
		this.getParameterValues().put(param, value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
