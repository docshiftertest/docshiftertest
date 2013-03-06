package com.docbyte.docshifter.messaging.topic.publisher;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import com.docbyte.docshifter.messaging.AbstractJMSConnection;
import com.docbyte.docshifter.messaging.IMessageSenderOrPublisher;

public abstract class AbstractJMSPublisher extends AbstractJMSConnection implements IMessageSenderOrPublisher  {

	protected TopicConnection connection = null;
	protected TopicSession session = null;
	protected Topic destination;
	protected TopicPublisher producer = null;
	protected boolean started;
	@Override
	public Connection getConnection() {
		return connection;
	}
	public boolean isStarted() {
		return started;
	}
	public void setStarted(boolean started) {
		this.started = started;
	}
	public void close() {
		try {
			if (getConnection() != null)
				getConnection().close();
			setStarted(false);
		} catch (JMSException e) {}
	}
	public void setConnection(TopicConnection connection) {
		this.connection = connection;
	}
	
	@Override
	public abstract void run();

}
