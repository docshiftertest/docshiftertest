package com.docbyte.docshifter.messaging.factory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.management.JMException;
import javax.management.remote.JMXConnector;

public interface IConnectionFactory {

	public Connection createConnection() throws JMSException;

	public TopicConnection createTopicConnection() throws JMSException;

	public JMXConnector createAdminConnection() throws JMException;
}
