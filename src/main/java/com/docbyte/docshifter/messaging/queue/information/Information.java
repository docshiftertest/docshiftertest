package com.docbyte.docshifter.messaging.queue.information;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.messaging.factory.IConnectionFactory;
import com.docbyte.docshifter.messaging.factory.MessagingConnectionFactory;
import com.docbyte.docshifter.util.Logger;

public class Information 
{
	private String user;
	private String password;
	private String url;
	private String queueName;
	private Connection connection;
	private static int nrStarted=0;
	protected Session session = null;
	protected MessageProducer producer = null;
	protected String queueNameSuffix;
	protected MessageConsumer consumer = null;
	private int count = 0;

	
	
	public int getNumberOfMessages()
	{
		GeneralConfigurationBean config = ConfigurationServer.getGeneralConfiguration();
		user = config.getString(Constants.JMS_USER);
		password = config.getString(Constants.JMS_PASSWORD);
		url = config.getString(Constants.JMS_URL);
		queueName = config.getString(Constants.JMS_QUEUE).concat(getQueueNameSuffix());

		try {
			
			IConnectionFactory connectionFactory;
			connectionFactory = MessagingConnectionFactory.getConnectionFactory(user, password, url);
	
			connection = connectionFactory.createConnection();
			connection.start();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			QueueBrowser browser =  session.createBrowser(session.createQueue(queueName));
			@SuppressWarnings("rawtypes")
			Enumeration enumeration = browser.getEnumeration();
			
			while(enumeration.hasMoreElements())
			{
				enumeration.nextElement();
				//Logger.info(message.toString(),null);
				count++;
			}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		//RETURN MESSAGE COUNT DIVIDED BY 2 BECAUSE THERE IS A MESSAGE FROM THE QUEUE AND A MESSAGE FROM THE
		return count/2;
	}
		
		public String getQueueNameSuffix() {
			if (queueNameSuffix!=null)
				return queueNameSuffix;
			else
				return "";
		}
		
		public void close() {
			Logger.debug("Being asked to close Connection"+(nrStarted--), null);
		//	if(nrStarted<=0){
		//		nrStarted=0;
				Logger.debug("Closing Connection"+(nrStarted), null);
				try{
					if (producer != null)
						producer.close();
				} catch (Exception ex){
					Logger.warn("Exception while closing producer from connection"+(nrStarted), ex);
				}
				try{
					if (session != null)
						session.close();
				} catch (Exception ex){
					Logger.warn("Exception while closing session from connection"+(nrStarted), ex);
				}
				try{
					if (connection != null)
						connection.close();
				} catch (Exception ex){
					Logger.warn("Exception while closing connection"+(nrStarted), ex);
				}
		}

}
