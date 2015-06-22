package com.docbyte.docshifter.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ModuleConfiguration implements Serializable{

	private static final long serialVersionUID = 7666888692582226276L;
	
	private int id;
	private Module module;
	private String name;
	private String description;
	private Map<Parameter, String> parameterValues = new HashMap<Parameter, String>();
	
	
	
	
	public ModuleConfiguration() {}
	
	public ModuleConfiguration(int id, Module module, String name,
			String description, Map<Parameter, String> parameterValues) {
		super();
		this.id = id;
		this.module = module;
		this.name = name;
		this.description = description;
		this.parameterValues = parameterValues;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
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

	@JsonIgnore
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

	/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((module == null) ? 0 : module.hashCode());
		result = prime * result
				+ ((parameterValues == null) ? 0 : parameterValues.hashCode());
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
		ModuleConfiguration other = (ModuleConfiguration) obj;
		if (module == null) {
			if (other.module != null)
				return false;
		} else if (!module.equals(other.module))
			return false;
		if (parameterValues == null) {
			if (other.parameterValues != null)
				return false;
		} else if (!parameterValues.equals(other.parameterValues))
			return false;
		return true;
	}
	*/
	@SuppressWarnings("rawtypes")
	@JsonProperty("parameters")
	public List<Map> jsonParameterValues()
	{
		List<Map> parameters = new ArrayList<Map>();
		Map<String, String> parameter = null;
		for(java.util.Map.Entry<Parameter, String> entry: parameterValues.entrySet())
		{
		parameter = new HashMap<String, String>();
		parameter.put("id",String.valueOf(entry.getKey().getId()));
		parameter.put("value",entry.getValue());
		parameters.add(parameter);
		}
		return parameters;
	}

	public boolean compareTo(Object obj) {
		ModuleConfiguration moduleConf = (ModuleConfiguration) obj;

		boolean equals = true;
		
		for(Parameter param : this.parameterValues.keySet()){
			if(!parameterValues.get(param).equals(moduleConf.getParameterValues().get(param))){
				equals = false;
			}
		}
		
		return equals && this.module.equals(moduleConf.getModule());
	}
}
