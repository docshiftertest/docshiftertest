package com.docshifter.core.config.services;

import org.springframework.jms.core.JmsTemplate;

public interface IJmsTemplateFactory {
	public static final int DEFAULT_PRIORITY = 4;
	public static final int HIGHEST_PRIORITY = 9;

	/**
	 * Creates a new JmsTemplate instance based on the defined settings.
	 * @param priority The task priority
	 * @param queueReplyTimeout How many ms to wait for a reply from the queue
	 * @param timeToLive 0 to disable or the expire time in ms
	 * @return a new JmsTemplate instance.
	 */
	JmsTemplate create(int priority, long queueReplyTimeout, long timeToLive);
}
