package com.docshifter.core.config.services;

import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

public class JmsTemplateFactory implements IJmsTemplateFactory {
	public static final int DEFAULT_PRIORITY = 4;
	public static final int HIGHEST_PRIORITY = 9;
	private final ConnectionFactory connectionFactory;

	public JmsTemplateFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public JmsTemplate create(int priority, long queueReplyTimeout) {
		JmsTemplate template = new JmsTemplate(connectionFactory);
		template.setPriority(priority);
		template.setReceiveTimeout(queueReplyTimeout);
		// Set if the QOS values (deliveryMode, priority, timeToLive) should be used for sending a message
		template.setExplicitQosEnabled(true);
		template.setDeliveryPersistent(true);
		// JMS tuning  - http://activemq.apache.org/components/artemis/documentation/1.3.0/perf-tuning.html
		template.setMessageTimestampEnabled(false);
		return template;
	}
}
