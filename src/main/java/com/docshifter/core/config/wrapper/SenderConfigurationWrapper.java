package com.docshifter.core.config.wrapper;


import com.docshifter.core.config.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SenderConfigurationWrapper extends NodeWrapper {

	public SenderConfigurationWrapper(Node n, ChainConfigurationRepository chainConfigurationRepository) {
		super(n, chainConfigurationRepository);
	}
	
	public SenderConfigurationWrapper() {
		super();
	}

	public List<ReceiverConfigurationWrapper> getApplicableReceiverConfigBeans(){
		final List<ReceiverConfigurationWrapper> list = new ArrayList<ReceiverConfigurationWrapper>();

		getNode().iterateOverNode( n-> {
				ReceiverConfigurationWrapper b = new ReceiverConfigurationWrapper(n, chainConfigurationRepository);
				list.add(b);
		});
		
		return list;
	}
	
	public String getString(String name) {
		return getString(name, null);
	}
	
	public String getString(String name, String defaultValue) {
		//TODO: add general config params id necessary
		Map<String, String> allParams = getModuleWrapper().params;
		
		if (allParams.containsKey(name)) {
			return allParams.get(name);
		} else {
			return defaultValue;
		}
		
	}
	
	public int getInt(String name) {
		return getInt(name, 0);
	}

	public int getInt(String name, int defaultValue){
		Map<String, String> allParams = getModuleWrapper().params;
		
		if (allParams.containsKey(name)) {
			return Integer.parseInt(allParams.get(name));
		} else {
			return defaultValue;
		}
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
