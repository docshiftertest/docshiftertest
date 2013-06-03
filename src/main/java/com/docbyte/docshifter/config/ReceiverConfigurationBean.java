package com.docbyte.docshifter.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.ReceiverConfiguration;

/**
 * Value object which represents a ReceiverConfiguration. Code to communicated with the configuration server should be put in here.
 * 
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */

public class ReceiverConfigurationBean {
	private String name;
	private String description;
	private Map<Integer,ModuleBean> transformationModules;
	private Set<ModuleBean> releaseModules;
	
	public ReceiverConfigurationBean(ReceiverConfiguration config){
		this.name = config.getName();
		this.description = config.getDescription();
		Iterator<ModuleConfiguration> releaseConfigs = config.getReleaseConfiguration().iterator();
		releaseModules = new HashSet<ModuleBean>();
		
		while(releaseConfigs.hasNext()){
			releaseModules.add(new ModuleBean(releaseConfigs.next()));
		}
		
		transformationModules = new HashMap<Integer, ModuleBean>();
		for(Entry<Integer, ModuleConfiguration> transmodule:config.getTransformationConfiguration().entrySet())
			transformationModules.put(transmodule.getKey(),new ModuleBean(transmodule.getValue()));
	}
	
	/**
	 * @return an iterator over the releaseModules
	 */
	public Iterator<ModuleBean> getReleaseModules() {
		return releaseModules.iterator();
	}
	
	/**
	 * @return the transformationModules
	 */
	public Map<Integer,ModuleBean> getTransformationModules() {
		return transformationModules;
	}

	/**
	 * @param transformationModules the transformationModules to set
	 */
	public void setTransformationModule(Map<Integer,ModuleBean> transformationModules) {
		this.transformationModules = transformationModules;
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
	
	/**
	 *
	 * Hardcoded filling in of the values, should actually get filled in with Hibernate info.
	 *
	 * @param long uid			a long representing the UID of the requested SenderConfiguration.
	 */
	/*
	public ReceiverConfigurationBean(long id){
		ReceiverConfiguration config = receiverConfigurationDAO.get(id);
		
			
		releaseModules = new HashSet<ModuleBean>();
		releaseModules.add(new ModuleBean(i));
	}
	*/
}
