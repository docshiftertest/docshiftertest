package com.docbyte.docshifter.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.docbyte.docshifter.model.dao.ChainConfigurationDAO;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.SenderConfiguration;

/**
 * Value object which represents a SenderConfiguration. Code to communicated with the configuration server should be put in here.
 * 
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */
public class SenderConfigurationBean {
	private ModuleBean inputModule;
	private ChainConfigurationDAO chainConfigurationDAO = new ChainConfigurationDAO();
	private long id;
	
	public SenderConfigurationBean() { }

	public SenderConfigurationBean(SenderConfiguration config){		
		inputModule = new ModuleBean(config.getInputConfiguration());
		this.id = config.getId();
	}
	
	/**
	 * Method that provides a List of ReceiverConfigurationBean objects representing all actions linked to the given Task.
	 * Should come from ConfigurationServer
	 */	
	public List<ReceiverConfigurationBean> getApplicableReceiverConfigBeans(){
		List<ReceiverConfigurationBean> list = new ArrayList<ReceiverConfigurationBean>();
		
		List<ChainConfiguration> transformationConfigs = chainConfigurationDAO.get();
		
		for(ChainConfiguration c : transformationConfigs){
			if(c.isEnabled() && c.getSenderConfiguration().getId() == id){
				list.add(new ReceiverConfigurationBean(c.getReceiverConfiguration()));
			}
		}
		
		return list;
	}
	//make belief:
	//content = timer, 
	public String getString(String name) {
		Map<String, String> allParams = inputModule.params;
		allParams.putAll(ConfigurationServer.getGeneralConfiguration().params);
		return (String)allParams.get(name);
	}
	//make belief:
	public int getInt(String name){
		Map<String, String> allParams = inputModule.params;
		allParams.putAll(ConfigurationServer.getGeneralConfiguration().params);
		return Integer.parseInt((String)allParams.get(name));
	}
	//make belief:
	public String getName(){
		return inputModule.getName();
	}

	/**
	 * @return the inputModule
	 */
	public ModuleBean getInputModule() {
		return inputModule;
	}

	/**
	 * @param inputModule the inputModule to set
	 */
	public void setInputModule(ModuleBean inputModule) {
		this.inputModule = inputModule;
	}

	/**
	 * @return the iD
	 */
	public long getID() {
		return id;
	}
}
