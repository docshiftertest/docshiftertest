package com.docshifter.core.config.entities;

import com.docshifter.core.security.Encrypted;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyClass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
public class ModuleConfiguration implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	private Module module;
	private String name;
	private String description;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@JsonIgnore
	@ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
	@MapKeyClass(Parameter.class)
	@Encrypted
	private Map<Parameter, String> parameterValues = new HashMap<>();

	public ModuleConfiguration() {}

	public ModuleConfiguration(long id, Module module, String name,
							   String description, Map<Parameter, String> parameterValues) {
		super();
		this.id = id;
		this.module = module;
		this.name = name;
		this.description = description;
		this.parameterValues =parameterValues;
	}

	public ModuleConfiguration(Module module, String name,
							   String description, Map<Parameter, String> parameterValues) {
		super();
//		this.id = id;
		this.module = module;
		this.name = name;
		this.description = description;
		this.parameterValues = parameterValues;
	}

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

	@ManyToOne(
			cascade = CascadeType.PERSIST,
			fetch = FetchType.EAGER
	)
	@JoinColumn(name = "MODULEID")
	public Module getModule()
	{
		return module;
	}


	public void setParameterValues(Map<Parameter, String> parameterValues)
	{
		this.parameterValues = parameterValues;
	}

	@Transient
	public void setParameterValue(Parameter param, String value)
	{
		for(Parameter existingParam : this.getParameterValues().keySet())
		{
			if (existingParam.getId() == param.getId())
				this.getParameterValues().put(existingParam, value);
		}
	}



	public Map<Parameter, String> getParameterValues()
	{
		return parameterValues;
	}

	@Transient
	public void addParameterValue(Parameter param, String value)
	{
		this.getParameterValues().put(param,value);
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

	@SuppressWarnings("rawtypes")
	@JsonProperty("parameters")
	@Transient
	public List<Map> jsonParameterValues()
	{
		List<Map> parameters = new ArrayList<Map>();
		Map<String, String> parameter = null;

		for(Map.Entry<Parameter, String> entry: parameterValues.entrySet())
		{
			parameter = new HashMap<String, String>();
			parameter.put("id", String.valueOf(entry.getKey().getId()));
			parameter.put("value", entry.getValue());
			
			parameters.add(parameter);
		}

		return parameters;
	}

	public boolean compareTo(Object obj) {
		ModuleConfiguration moduleConf = (ModuleConfiguration) obj;

		boolean equals = true;

		for (Parameter param : this.parameterValues.keySet()) {
			if (!parameterValues.get(param).equals(moduleConf.getParameterValues().get(param))) {
				equals = false;
			}
		}

		return equals && this.module.equals(moduleConf.getModule());
	}

	@Override
	public String toString() {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("{");
		sBuf.append("\"ID\": ");
		sBuf.append(this.getId());
		sBuf.append(", \"Name\": \"");
		sBuf.append(this.getName());
		sBuf.append("\", \"Description\": \"");
		sBuf.append(this.getDescription());
		sBuf.append("\", \"ParameterValues\": ");
		if (this.getParameterValues() == null) {
			sBuf.append("null");
		}
		else {
			sBuf.append("[");
			for (Parameter param : this.getParameterValues().keySet()) {
				sBuf.append("{\"Parameter\": ");
				sBuf.append(param.toString());
				sBuf.append(", \"value\": \"");
				sBuf.append(this.getParameterValues().get(param));
				sBuf.append("\"}, ");
			}
			if (this.getParameterValues().size() > 0) {
				sBuf.setLength(sBuf.length() - 2);
			}
			sBuf.append("]");
		}
		sBuf.append(", ");
		sBuf.append("\"Module\": ");
		sBuf.append(this.getModule().toString());
		sBuf.append("}");
		return sBuf.toString();
	}
}
