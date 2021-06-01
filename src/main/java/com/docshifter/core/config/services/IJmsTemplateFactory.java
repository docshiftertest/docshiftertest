package com.docshifter.core.config.services;

import org.springframework.jms.core.JmsTemplate;

public interface IJmsTemplateFactory {
	JmsTemplate create(int priority, long queueReplyTimeout);
}
