package com.docbyte.docshifter.messaging.factory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.TopicConnection;
import javax.management.JMException;
import javax.management.remote.JMXConnector;

public interface IConnectionFactory {

	Connection createConnection() throws JMSException;

	TopicConnection createTopicConnection() throws JMSException;

	JMXConnector createAdminConnection() throws JMException;
}
