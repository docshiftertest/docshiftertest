package com.docbyte.docshifter.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.docbyte.docshifter.config.test.NodeCallable;
import com.docbyte.docshifter.model.vo.Node;

public class SenderConfigurationBean extends NodeBean {

	public SenderConfigurationBean(Node n) {
		super(n);
	}
	
	public SenderConfigurationBean() {
		super();
	}

	public List<ReceiverConfigurationBean> getApplicableReceiverConfigBeans(){
		final List<ReceiverConfigurationBean> list = new ArrayList<ReceiverConfigurationBean>();
		
		getNode().iterateOverNode(new NodeCallable() {
			
			@Override
			public void exitingChildNodes() {}
			
			@Override
			public void enteringChildNodes() {}
			
			@Override
			public void call(Node n) {
				ReceiverConfigurationBean b = new ReceiverConfigurationBean(n);
				list.add(b);
			}
		});
		
		return list;
	}
	
	public String getString(String name) {
		Map<String, String> allParams = getModuleBean().params;
		allParams.putAll(ConfigurationServer.getGeneralConfiguration().params);
		return (String)allParams.get(name);
	}

	public int getInt(String name){
		Map<String, String> allParams = getModuleBean().params;
		allParams.putAll(ConfigurationServer.getGeneralConfiguration().params);
		return Integer.parseInt((String)allParams.get(name));
	}

	public String getName(){
		return getModuleBean().getName();
	}

	public String getConfigName(){
		return getModuleBean().getConfigName();
	}
	
	
}
