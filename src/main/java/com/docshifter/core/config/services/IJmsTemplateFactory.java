package com.docshifter.core.config.services;

import org.springframework.jms.core.JmsTemplate;

public interface IJmsTemplateFactory {
	public static final int DEFAULT_PRIORITY = 4;
	public static final int HIGHEST_PRIORITY = 9;

	JmsTemplate create(int priority, long queueReplyTimeout);
}
