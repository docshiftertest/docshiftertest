package com.docbyte.docshifter.messaging.factory;


import com.aspose.email.system.exceptions.NotImplementedException;
import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;

import javax.jms.JMSException;

public abstract class MessagingConnectionFactory {
		
	public static IConnectionFactory getConnectionFactory(String user, String password, String url) throws JMSException{
		GeneralConfigurationBean config = ConfigurationServer.getGeneralConfiguration();
		return getConnectionFactory (user, password, url, config.getString(Constants.MQ_SYSTEM));
	}


	public static IConnectionFactory getConnectionFactory(String user, String password, String url, String jmsSystem) throws JMSException{
		switch (jmsSystem.toUpperCase()) {
			case "ACTIVEMQ":
				return new ActiveMQConnectionFactory(user, password, url);
			case "OPENMQ":
				return new OpenMQConnectionFactory(user, password, url);
			default:
				throw new NotImplementedException("System not yet implemented");
		}

	}

}
