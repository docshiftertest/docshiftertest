package com.docbyte.docshifter.messaging.factory;


import javax.jms.JMSException;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.util.Logger;

public abstract class MessagingConnectionFactory {
		
	public static IConnectionFactory getConnectionFactory(String user, String password, String url) throws JMSException{
		GeneralConfigurationBean config = ConfigurationServer.getGeneralConfiguration();
		if (config.getString(Constants.JMS_SYSTEM)==null){
			Logger.error("No JMSSYSTEM variable in configuration, returning OpenMQConnectionFactory", null);
			return new OpenMQConnectionFactory(user, password, url);
		} else if (config.getString(Constants.JMS_SYSTEM)!=null &&
				config.getString(Constants.JMS_SYSTEM).equalsIgnoreCase("OpenMQ")){
			return new OpenMQConnectionFactory(user, password, url);
		} else {
			Logger.debug("No valid JMSSYSTEM set in database", null);
			return null;
		}
	}

}
