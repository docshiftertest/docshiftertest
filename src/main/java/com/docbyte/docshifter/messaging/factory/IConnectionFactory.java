package com.docbyte.docshifter.messaging.factory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TopicConnection;

public interface IConnectionFactory {
	
	public Connection createConnection() throws JMSException;

	public TopicConnection createTopicConnection() throws JMSException;

}
