package com.docshifter.core.config.wrapper;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.docshifter.core.config.domain.ChainConfigurationRepository;
import com.docshifter.core.config.domain.Node;

public class SenderConfigurationWrapper extends NodeWrapper {

	private static final Logger logger = Logger.getLogger(SenderConfigurationWrapper.class);

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
		logger.debug("Calling getString(" + name + ", null)");
		return getString(name, null);
	}
	
	public String getString(String name, String defaultValue) {
		//TODO: add general config params id necessary
		Map<String, String> allParams = getModuleWrapper().params;
		logger.debug("getString(" + name + ", " + defaultValue + ") called");
		
		if (allParams.containsKey(name)) {
			logger.debug("containsKey so returning: " + allParams.get(name)); 
			return allParams.get(name);
		} else {
			logger.debug("returning (defaultValue): " + defaultValue);
			return defaultValue;
		}
		
	}
	
	public int getInt(String name) {
		logger.debug("Calling getInt(" + name + ", 0)");
		return getInt(name, 0);
	}

	public int getInt(String name, int defaultValue){
		Map<String, String> allParams = getModuleWrapper().params;
		logger.debug("getInt(" + name + ", " + defaultValue + ") called");
		int result = defaultValue;
		try {
			if (allParams.containsKey(name)) {
				result = Integer.parseInt(allParams.get(name));
			}
		}
		catch (NumberFormatException niffy) {
			// Just log it, we already set the default value
			logger.warn("Used default value: " + defaultValue + " as we couldn't parse the provided value: [" + allParams.get(name) + "] for parameter: [" + name + "]");
			logger.trace(niffy);
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
