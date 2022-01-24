package com.docshifter.core.config.services;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JmsDestinationAccessor;

import javax.jms.ConnectionFactory;

public class JmsTemplateFactory implements IJmsTemplateFactory {
	private final ConnectionFactory connectionFactory;

	public JmsTemplateFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public JmsTemplate create(int priority, long queueReplyTimeout,long timeToLive) {
		JmsTemplate template = new JmsTemplate(connectionFactory);
		template.setPriority(priority);
		if (queueReplyTimeout <= 0) {
			queueReplyTimeout = JmsDestinationAccessor.RECEIVE_TIMEOUT_INDEFINITE_WAIT;
		}
		template.setReceiveTimeout(queueReplyTimeout);
		// Set if the QOS values (deliveryMode, priority, timeToLive) should be used for sending a message
		template.setExplicitQosEnabled(true);
		template.setDeliveryPersistent(true);
		// JMS tuning  - http://activemq.apache.org/components/artemis/documentation/1.3.0/perf-tuning.html
		template.setMessageTimestampEnabled(false);
		if (timeToLive > 0) {
			template.setTimeToLive(timeToLive);
		}
		return template;
	}

}
