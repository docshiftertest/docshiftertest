package com.docbyte.docshifter.config.test;

import com.docshifter.core.config.ConfigurationServer;
import com.docshifter.core.config.GeneralConfigurationBean;
import com.docshifter.core.config.SenderConfigurationBean;

import java.util.List;
import java.util.Set;

public class Test {

	public static void main(String[] args) {
		testConfigurationBeans();
		testGeneralConfigurationBean();
	}

	private static void testConfigurationBeans(){
		/**
		 * TODO: Check what this test is supposed to do
 		 */
//		Set<SenderConfigurationBean> configs = ConfigurationServer.getEnabledSenderConfigurations();
//		System.out.println("# enabled sender configs: " +configs.size());
//
//		List<SenderConfigurationBean> senders = ConfigurationServer.getSenderConfiguration("inputmoduleClass");
//
//		for(SenderConfigurationBean config : senders){
//
//			System.out.println("# applicable receivers for sender: " + config.getNode().getTotalChildNodesCount());
//
//
//			config.getNode().iterateOverNode(n -> {
//					for(int i = 0; i < amountOfTabs; i++)
//						System.out.print("\t");
//					System.out.println(" - " + n.getTotalChildNodesCount());
//				});
//
//		}
//
//		SenderConfigurationBean ws_sender = ConfigurationServer.getSenderConfigurationWS("com.docbyte.docshifter.sender.webservice.WebServiceSender", "pdf");
//		System.out.println(ws_sender.getModuleBean().getName());
	}
	
	private static void testGeneralConfigurationBean(){
	//	GeneralConfigurationBean bean = new GeneralConfigurationBean();
		
	//	System.out.println("jms_url: " +bean.getString("jms_url"));
	}
}
