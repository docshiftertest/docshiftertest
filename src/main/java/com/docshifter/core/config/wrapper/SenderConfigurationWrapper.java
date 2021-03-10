package com.docshifter.core.config.wrapper;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import com.docshifter.core.config.repositories.ChainConfigurationRepository;
import com.docshifter.core.config.entities.Node;

@Log4j2
public class SenderConfigurationWrapper extends NodeWrapper {

	public SenderConfigurationWrapper(Node node, ChainConfigurationRepository chainConfigurationRepository) {
		super(node, chainConfigurationRepository);
	}
	
	public SenderConfigurationWrapper() {
		super();
	}

	public List<ReceiverConfigurationWrapper> getApplicableReceiverConfigBeans(){
		final List<ReceiverConfigurationWrapper> list = new ArrayList<ReceiverConfigurationWrapper>();

		getNode().iterateOverNode( node-> {
				ReceiverConfigurationWrapper b = new ReceiverConfigurationWrapper(node, chainConfigurationRepository);
				list.add(b);
		});
		
		return list;
	}
	
	public String getString(String name) {
		log.debug("Calling getString({}, null)", name);
		return getString(name, null);
	}
	
	public String getString(String name, String defaultValue) {
		//TODO: add general config params id necessary
		Map<String, String> allParams = getModuleWrapper().params;
		log.debug("getString({}, {}) called", name, defaultValue);
		
		if (allParams.containsKey(name)) {
			log.debug("containsKey so returning: {}", allParams.get(name));
			return allParams.get(name);
		} else {
			log.debug("returning (defaultValue): {}", defaultValue);
			return defaultValue;
		}
		
	}
	
	public int getInt(String name) {
		log.debug("Calling getInt({}, 0)", name);
		return getInt(name, 0);
	}

	public int getInt(String name, int defaultValue){
		Map<String, String> allParams = getModuleWrapper().params;
		log.debug("getInt({}, {}) called", name, defaultValue);
		int result = defaultValue;
		try {
			if (allParams.containsKey(name)) {
				result = Integer.parseInt(allParams.get(name));
			}
		}
		catch (NumberFormatException niffy) {
			// Just log it, we already set the default value
			log.warn("Used default value: {} as we couldn't parse the provided value: [{}] for parameter: [{}]",
					defaultValue, allParams.get(name), name);
			log.trace(niffy);
		}
		return result;
	}
	
	public boolean getBoolean(String name) {
		return getBoolean(name, false);
	}
	
	public boolean getBoolean(String name, boolean defaultValue){
		Map<String, String> allParams = getModuleWrapper().params;
		
		if (allParams.containsKey(name)) {
			return Boolean.parseBoolean(allParams.get(name));
		} else {
			return defaultValue;
		}
	}

	public String getName(){
		return getModuleWrapper().getName();
	}
	
	
}
