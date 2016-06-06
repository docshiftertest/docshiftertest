package com.docbyte.docshifter.messaging.queue.sender;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.messaging.AbstractJMSConnection;
import com.docbyte.docshifter.messaging.IMessageSenderOrPublisher;
import com.docbyte.docshifter.messaging.factory.IConnectionFactory;
import com.docbyte.docshifter.messaging.factory.MessagingConnectionFactory;
import com.docbyte.docshifter.util.Logger;

import javax.jms.*;

public abstract class AbstractJMSSender extends AbstractJMSConnection implements IMessageSenderOrPublisher{
	public static int retriesLeft=999;
	public static final int RETRYDELAY=5;
	
	protected Connection connection = null;
	protected Session session = null;
	protected Destination destination;
	protected MessageProducer producer = null;
	private boolean firstRun = true;
	//TODO: why static ???
	private int nrStarted=0;

	public boolean isStarted() {
		return nrStarted>0;
	}
	public int getNrStarted() {
		return nrStarted;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void retry(Exception ex){

			Logger.error("JMS Exception occured, make sure the JMS Provider is running correctly. Retry in "+RETRYDELAY+" minutes", ex);
			try {
				Thread.sleep(RETRYDELAY * 60000);
			} catch (InterruptedException e1) {
				Logger.error("Thread failed to put in sleepmodus. AbstractJMSSender", e1);
			}
			run();
	}

	public void close() {
		//Logger.debug("Being asked to close Connection"+(nrStarted--), null);
		Logger.info("Start to run ... DONE exiting ="+(nrStarted), null);
/*
		if(nrStarted>0){
			//nrStarted=0;
			Logger.info("Closing Connection"+(nrStarted), null);
			try{
				if (producer != null){
					producer.close();
					Logger.info("Producer closed nr="+(nrStarted), null);}
			} catch (Exception ex){
				Logger.warn("Exception while closing producer from connection"+(nrStarted), ex);
			}
			try{
				if (session != null){
					session.close();
					Logger.info("session closed nr="+(nrStarted), null);}
			} catch (Exception ex){
				Logger.warn("Exception while closing session from connection"+(nrStarted), ex);
			}
			try{
				if (connection != null){
					connection.close();
					Logger.info("connection closed nr="+(nrStarted), null);}
			} catch (Exception ex){
				Logger.warn("Exception while closing connection"+(nrStarted), ex);
			}
			nrStarted--;
			Logger.info("nrStarted = " + nrStarted, null);
		} */

	}

	@Override
	public void run() {

		Logger.info("Start to run ... ="+(nrStarted), null);
		if (firstRun) {
			firstRun = false;
			init();
		}

	}

	public void init(){

		GeneralConfigurationBean config = ConfigurationServer.getGeneralConfiguration();
		user = config.getString(Constants.MQ_USER);
		password = config.getString(Constants.MQ_PASSWORD);
		url = config.getString(Constants.MQ_URL);
		queueName = config.getString(Constants.MQ_QUEUE).concat(getQueueNameSuffix());

		try{
			IConnectionFactory connectionFactory = MessagingConnectionFactory.getConnectionFactory(user, password, url);
			connection = connectionFactory.createConnection();
			connection.start();
			nrStarted++;
			Logger.debug("Started Connection nr="+(nrStarted), null);
			try{
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				Logger.info("session started nr="+(nrStarted), null);
			}catch(JMSException e){
				//close();
				Logger.error("session ERROR nr="+(nrStarted), null);
				retry(e);
			}

			destination = session.createQueue(queueName);
			producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			firstRun = false;


		} catch (JMSException e){
			Logger.info("Error, creating connection nr EXCEPTION="+(nrStarted), null);
			//close();
			retry(e);
		}

	}
}