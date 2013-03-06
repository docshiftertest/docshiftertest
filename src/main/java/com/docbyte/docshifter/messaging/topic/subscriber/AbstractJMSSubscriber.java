package com.docbyte.docshifter.messaging.topic.subscriber;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.messaging.AbstractJMSConnection;
import com.docbyte.docshifter.messaging.IMessageReceiverOrSubscriber;
import com.docbyte.docshifter.messaging.factory.IConnectionFactory;
import com.docbyte.docshifter.messaging.factory.MessagingConnectionFactory;
import com.docbyte.docshifter.util.Logger;


abstract public class AbstractJMSSubscriber extends AbstractJMSConnection implements IMessageReceiverOrSubscriber {
	public static int retriesLeft=3;
	public static final int RETRYDELAY=5;
	
	protected TopicConnection connection=null;
	protected TopicSession session=null;
	protected Topic destination=null;
	protected TopicSubscriber consumer=null;
	@Override
	public void run() {
		GeneralConfigurationBean config = ConfigurationServer.getGeneralConfiguration();
		
		if(config != null){
			user = config.getString(Constants.JMS_USER);
			password = config.getString(Constants.JMS_PASSWORD);
			url = config.getString(Constants.JMS_URL);
			
			try {
				IConnectionFactory connectionFactory = MessagingConnectionFactory.getConnectionFactory(user, password, url);
				connection = connectionFactory.createTopicConnection();
				connection.start();
				
				connection.setExceptionListener(this);
				
				session = connection.createTopicSession(false, ackMode);
				destination = session.createTopic("notificationTopic");
				
				consumer = session.createSubscriber(destination);
				consumer.setMessageListener(this); //async
			} catch (JMSException e){
				retry(e);
				//Logger.error("JMSSub: JMS Exception occured, make sure the JMS provider is running correctly.", e);
			}
		}
	}
	
	@Override
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(TopicConnection connection) {
		this.connection = connection;
	}
	
	public void onException(JMSException e) {
		try{
			if(connection !=null)
				connection.close();
		} catch (JMSException ex){	
		} finally {
			retry(e);
		}
	}
	
	public void retry(Exception ex){
		//if(0<=retriesLeft){
			Logger.error("JMS Exception occured, make sure the JMS Provider is running correctly. Retry in "+RETRYDELAY+" minutes", ex);
			try {
				Thread.sleep(RETRYDELAY*60000);
			} catch (InterruptedException e1) {
			}
			run();
		//}
		//else {
			//Logger.error("JMS Exception occured, make sure the JMS Provider is running correctly. No more retries left, Shutting down...", ex);
			//System.exit(1);
		//}
	}
	
	
	
}
