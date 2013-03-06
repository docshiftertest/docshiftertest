package com.docbyte.docshifter.messaging.factory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TopicConnection;

import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.ConnectionFactory;

class OpenMQConnectionFactory implements IConnectionFactory {
	
	private String user;
	private String password;
	private ConnectionFactory connectionFactory;

	public OpenMQConnectionFactory(String user, String password, String url) throws JMSException {
		if (user!=null && user.length()!=0) 
			this.user = user;
		else
			this.user = "guest";
		if (password!=null && password.length()!=0)
			this.password = password;
		else
			this.password = "guest";
		if (url!=null && url.length()!=0){
			connectionFactory = new ConnectionFactory();
			// example url: "localhost:7676"
			connectionFactory.setProperty(ConnectionConfiguration.imqAddressList, url);
		}
	}

	public Connection createConnection() throws JMSException {
		return connectionFactory.createConnection(user, password);
	}

	public TopicConnection createTopicConnection() throws JMSException {
		return connectionFactory.createTopicConnection(user, password);
	}
}
