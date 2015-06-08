package com.docbyte.docshifter.config.test;

import java.util.List;
import java.util.Set;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.config.SenderConfigurationBean;
import com.docbyte.docshifter.model.vo.Node;

public class Test {

	public static void main(String[] args) {
		testConfigurationBeans();
		testGeneralConfigurationBean();
	}

	private static void testConfigurationBeans(){
		Set<SenderConfigurationBean> configs = ConfigurationServer.getEnabledSenderConfigurations();
		System.out.println("# enabled sender configs: " +configs.size());
		
		List<SenderConfigurationBean> senders = ConfigurationServer.getSenderConfiguration("inputmoduleClass");
		
		for(SenderConfigurationBean config : senders){
			
			System.out.println("# applicable receivers for sender: " + config.getNode().getTotalChildNodesCount());
			
			
			config.getNode().iterateOverNode(new NodeCallable(){
				int amountOfTabs = 0;
				
				public void call(Node n){
					for(int i = 0; i < amountOfTabs; i++)
						System.out.print("\t");
					System.out.println(" - " + n.getTotalChildNodesCount());
				}
				
				public void enteringChildNodes(){
					amountOfTabs++;
				}
				
				public void exitingChildNodes(){
					amountOfTabs--;
				}
			});
			
		}
		
		SenderConfigurationBean ws_sender = ConfigurationServer.getSenderConfigurationWS("com.docbyte.docshifter.sender.webservice.WebServiceSender", "pdf");
		System.out.println(ws_sender.getModuleBean().getName());
	}
	
	private static void testGeneralConfigurationBean(){
		GeneralConfigurationBean bean = new GeneralConfigurationBean();
		
		System.out.println("jms_url: " +bean.getString("jms_url"));
	}
}
