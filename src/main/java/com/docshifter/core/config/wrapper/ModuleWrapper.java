package com.docshifter.core.config.wrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.docshifter.core.config.domain.*;

public class ModuleWrapper {

	protected Map<String, String> params;
	private String type;
	private String name;
	private String description;
	
	public ModuleWrapper(ModuleConfiguration config){
		this.type = config.getModule().getType();
		//this.name = config.getName();
		this.name = config.getModule().getClassname();
		this.description = config.getDescription();
		
		params = new HashMap<String, String>();


		Iterator<Parameter> it = config.getParameterValues().keySet().iterator();
		while(it.hasNext()){
			Parameter key = it.next();
			String value = config.getParameterValues().get(key);
			params.put(key.getName(), value);
		}
	}
	
	public String toString(){
		return name;
	}
	/**
	 * @param name the name of the requested parameter
	 * @return the String value linked to the requested parameter
	 */
	public String getString(String name) {
		return params.get(name);
	}
	/**
	 * @param name the name of the requested parameter
	 * @return the int value linked to the requested parameter
	 */
	public int getInt(String name){
		return Integer.parseInt(params.get(name));
	}
	/**
	 * @param name the name of the requested parameter
	 * @return the boolean value linked to the requested parameter
	 */
	public Boolean getBoolean(String name){
		if (params.get(name) == null) {
			return null;
		}
		return Boolean.parseBoolean(params.get(name));
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	
}
