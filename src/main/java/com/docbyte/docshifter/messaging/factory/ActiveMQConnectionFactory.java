package com.docbyte.docshifter.messaging.factory;



import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.management.JMException;
import javax.management.remote.JMXConnector;

@SuppressWarnings("unused")
class ActiveMQConnectionFactory implements IConnectionFactory {

	private String user;
	private String password;
	private org.apache.activemq.ActiveMQConnectionFactory connectionFactory;
	//private ActiveMQ acf;

	public ActiveMQConnectionFactory(String user, String password, String url) throws JMSException {
		if (user!=null && user.length()!=0) 
			this.user = user;
		else
			this.user = "guest";
		if (password!=null && password.length()!=0)
			this.password = password;
		else
			this.password = "guest";
		if (url!=null && url.length()!=0){
			connectionFactory = new org.apache.activemq.ActiveMQConnectionFactory();
//			acf = new AdminConnectionFactory();
			// example url: "localhost:7676"
			connectionFactory.setBrokerURL(url);
//			acf.setProperty(AdminConnectionConfiguration.imqAddress, url);
		}
	}

	public Connection createConnection() throws JMSException {
		return connectionFactory.createConnection(user, password);
	}
	
	public JMXConnector createAdminConnection() throws JMException {
		return null;
	}

	public TopicConnection createTopicConnection() throws JMSException {
		return connectionFactory.createTopicConnection(user, password);
	}
}
