package com.docbyte.docshifter.config.test;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.config.ModuleBean;
import com.docbyte.docshifter.config.ReceiverConfigurationBean;
import com.docbyte.docshifter.config.SenderConfigurationBean;

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
			System.out.println("sender: " +config.getName());
			
			List<ReceiverConfigurationBean> receivers = config.getApplicableReceiverConfigBeans();
			System.out.println("# applicable receivers for sender: " +receivers.size());
			
			for(ReceiverConfigurationBean bean : receivers){
				System.out.println("Release modules for receiver: " +bean.getName());
				Iterator<ModuleBean> it = bean.getReleaseModules();
				
				while(it.hasNext()){
					System.out.println(it.next().getName());
				}
			}
		}
		
		SenderConfigurationBean ws_sender = ConfigurationServer.getSenderConfigurationWS("com.docbyte.docshifter.sender.webservice.WebServiceSender", "pdf");
		System.out.println(ws_sender.getName());
	}
	
	private static void testGeneralConfigurationBean(){
		GeneralConfigurationBean bean = new GeneralConfigurationBean();
		
		System.out.println("jms_url: " +bean.getString("jms_url"));
	}
}
