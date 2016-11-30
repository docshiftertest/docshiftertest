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
		Map<String, String> allParams = getModuleWrapper().params;
		//TODO: add general config params id necessary
		return allParams.get(name);
	}

	public int getInt(String name){
		Map<String, String> allParams = getModuleWrapper().params;
		//TODO: add general config params id necessary
		return Integer.parseInt(allParams.get(name));
	}

	public String getName(){
		return getModuleWrapper().getName();
	}
	
	
}
