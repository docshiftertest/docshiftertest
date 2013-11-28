package com.docbyte.docshifter.messaging.factory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.management.JMException;
import javax.management.remote.JMXConnector;

import com.sun.messaging.AdminConnectionConfiguration;
import com.sun.messaging.AdminConnectionFactory;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.ConnectionFactory;
import com.sun.messaging.jms.management.server.ConsumerInfo;

@SuppressWarnings("unused")
class OpenMQConnectionFactory implements IConnectionFactory {
	
	private String user;
	private String password;
	private ConnectionFactory connectionFactory;
	private AdminConnectionFactory acf;

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
			acf = new AdminConnectionFactory();
			// example url: "localhost:7676"
			connectionFactory.setProperty(ConnectionConfiguration.imqAddressList, url);
			acf.setProperty(AdminConnectionConfiguration.imqAddress, url);
		}
	}

	public Connection createConnection() throws JMSException {
		return connectionFactory.createConnection(user, password);
	}
	
	public JMXConnector createAdminConnection() throws JMException {
		return acf.createConnection(user,password);
	}

	public TopicConnection createTopicConnection() throws JMSException {
		return connectionFactory.createTopicConnection(user, password);
	}
}
