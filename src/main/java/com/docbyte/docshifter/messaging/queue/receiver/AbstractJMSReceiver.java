package com.docbyte.docshifter.messaging.queue.receiver;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.messaging.AbstractJMSConnection;
import com.docbyte.docshifter.messaging.IMessageReceiverOrSubscriber;
import com.docbyte.docshifter.messaging.factory.IConnectionFactory;
import com.docbyte.docshifter.messaging.factory.MessagingConnectionFactory;
import com.docbyte.docshifter.util.Logger;

import javax.jms.*;
import javax.management.JMException;
import javax.management.remote.JMXConnector;
import java.lang.IllegalStateException;

public abstract class AbstractJMSReceiver extends AbstractJMSConnection implements IMessageReceiverOrSubscriber {

	protected Session session = null;
	protected Destination destination = null;
	protected Connection connection = null;
	protected JMXConnector adminConnection = null;
	protected MessageConsumer consumer = null;


	/* (non-Javadoc)
	 * @see com.docbyte.docshifter.messaging.queue.JMSSubscriberOrReceiver#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message message) {
		boolean result= false;
		try {
			result = controller.onMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(!result){
			Logger.warn("Failed to complete the requested operation",null);
		}else if(ackMode==Session.CLIENT_ACKNOWLEDGE){
			try {
				message.acknowledge();
			} catch (JMSException e) {
				Logger.warn("The JMS provider failed to acknowledge " +
						"the messages due to some internal error.", e);
			} catch (IllegalStateException e){
				Logger.warn("Tried to acknowledge when the " +
						"session was already closed.",e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.docbyte.docshifter.messaging.queue.JMSSubscriberOrReceiver#run()
	 */
	@Override
	public void run() {

		GeneralConfigurationBean config = ConfigurationServer.getGeneralConfiguration();
		
		if(config != null){
			user = config.getString(Constants.MQ_USER);
			password = config.getString(Constants.MQ_PASSWORD);
			url = config.getString(Constants.MQ_URL);
			queueName = config.getString(Constants.MQ_QUEUE).concat(getQueueNameSuffix());
			
			try {
				IConnectionFactory connectionFactory = MessagingConnectionFactory.getConnectionFactory(user, password, url);
				connection = connectionFactory.createConnection();
				adminConnection = connectionFactory.createAdminConnection();
				connection.setExceptionListener(this);
				
				session = connection.createSession(false, ackMode);
				destination = session.createQueue(queueName);
				
				consumer = session.createConsumer(destination);
				consumer.setMessageListener(this); //async
				
				connection.start();
			} catch (JMSException e){
				//try again in 5 min
				retry(e);
			} catch (JMException e) {
				// TODO Auto-generated catch block
				retry(e);
			}
		}else {
			Logger.error("Could not start docShifter Receiver, config error",null);
		}
	}


	@Override
	public Connection getConnection() {
		return connection;
	}
	
	public JMXConnector getAdminConnection(){
		return adminConnection;
	}


	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public MessageConsumer getConsumer(){
		return consumer;
	}
	
	
	
	

}