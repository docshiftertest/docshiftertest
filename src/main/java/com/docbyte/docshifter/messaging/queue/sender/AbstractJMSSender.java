package com.docbyte.docshifter.messaging.queue.sender;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.messaging.AbstractJMSConnection;
import com.docbyte.docshifter.messaging.IMessageSenderOrPublisher;
import com.docbyte.docshifter.messaging.factory.IConnectionFactory;
import com.docbyte.docshifter.messaging.factory.MessagingConnectionFactory;
import com.docbyte.docshifter.util.Logger;

public abstract class AbstractJMSSender extends AbstractJMSConnection implements IMessageSenderOrPublisher  {
	public static int retriesLeft=999;
	public static final int RETRYDELAY=5;
	
	protected Connection connection = null;
	protected Session session = null;
	protected Destination destination;
	protected MessageProducer producer = null;
	
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
		//	Logger.error("JMS Exception occured, make sure the JMS Provider is running correctly. No more retries left, Shutting down...", ex);
		//System.exit(1);
		//}
	}

	private static int nrStarted=0;
//	private boolean started = false;

	/* (non-Javadoc)
	 * @see com.docbyte.docshifter.messaging.queue.JMSSenderOrPublisher#close()
	 */
	public void close() {
		//Logger.debug("Being asked to close Connection"+(nrStarted--), null);
		//if(nrStarted<=0){
			//nrStarted=0;
			Logger.debug("Closing Connection"+(nrStarted), null);
			try{
				if (producer != null){
					producer.close();
					Logger.info("Producer closed", null);}
			} catch (Exception ex){
				Logger.warn("Exception while closing producer from connection"+(nrStarted), ex);
			}
			try{
				if (session != null){
					session.close();
					Logger.info("session closed", null);}
			} catch (Exception ex){
				Logger.warn("Exception while closing session from connection"+(nrStarted), ex);
			}
			try{
				if (connection != null){
					connection.close();
					Logger.info("connection closed", null);}
			} catch (Exception ex){
				Logger.warn("Exception while closing connection"+(nrStarted), ex);
			}
			nrStarted--;
			Logger.info("nrStarted = " + nrStarted, null);
//		}
//		try{
//			if (connection != null)
//				connection.close();
//			started=false;
//		} catch (Exception ex){}
	}

	public boolean isStarted() {
		return nrStarted>0;
//		return started;
	}
	
	public static int getNrStarted() {
		return nrStarted;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}


	public void setConnection(Connection connection) {
		this.connection = connection;
	}


	/* (non-Javadoc)
	 * @see com.docbyte.docshifter.messaging.queue.JMSSenderOrPublisher#run()
	 */
	@Override
	public void run() {
		GeneralConfigurationBean config = ConfigurationServer.getGeneralConfiguration();
		user = config.getString(Constants.JMS_USER);
		password = config.getString(Constants.JMS_PASSWORD);
		url = config.getString(Constants.JMS_URL);
		queueName = config.getString(Constants.JMS_QUEUE).concat(getQueueNameSuffix());

		try{
			IConnectionFactory connectionFactory = MessagingConnectionFactory.getConnectionFactory(user, password, url);
			connection = connectionFactory.createConnection();
			connection.start();
			nrStarted++;
			Logger.debug("Started Connection"+(nrStarted), null);
			try{
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Logger.info("session started", null);
			}catch(JMSException e){
				close();
				retry(e);
				//e.printStackTrace();
				//session=connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			}
			
			destination = session.createQueue(queueName);

			producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);

		} catch (JMSException e){
			Logger.info("Error closing connection", null);
			close();
			retry(e);
			/*
			 * 
			Logger.error("JMSSender: JMS Exception occured, make sure the JMS provider is running correctly. Shutting down docShifter ...", e);
			//System.exit(1);
			*/
		}
//		GeneralConfigurationBean config = ConfigurationServer.getGeneralConfiguration();
//		user = config.getString(Constants.JMS_USER);
//		password = config.getString(Constants.JMS_PASSWORD);
//		url = config.getString(Constants.JMS_URL);
//		queueName = config.getString(Constants.JMS_QUEUE).concat(getQueueNameSuffix());
//		
//		try {
//			IConnectionFactory connectionFactory = MessagingConnectionFactory.getConnectionFactory(user, password, url);
//			connection = connectionFactory.createConnection();
//			connection.start();
//			
//			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//			destination = session.createQueue(queueName);
//			
//			producer = session.createProducer(destination);
//			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
//			
//			setStarted(true);
//		} catch (JMSException e){
//			Logger.error("JMS Exception occured, make sure the JMS provider is running correctly. Shutting down docShifter ...", e);
//			System.exit(1);
//		}
	}
	
	

}