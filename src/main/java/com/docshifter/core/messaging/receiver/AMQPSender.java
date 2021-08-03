package com.docshifter.core.messaging.receiver;

import com.docshifter.core.messaging.queue.receiver.IMessageSender;
import com.docshifter.core.config.services.IJmsTemplateFactory;
import com.docshifter.core.messaging.message.DocShifterMetricsReceiverMessage;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.springframework.jms.core.JmsTemplate;

/**
 * Created by Julian Isaac on 29.07.2021
 */

@Log4j2
public class AMQPSender implements IMessageSender {

	private final ActiveMQQueue docshifterMetricsQueue;
	private final JmsTemplate defaultJmsTemplate;
	private final IJmsTemplateFactory jmsTemplateFactory;
	private final int queueReplyTimeout;

	public AMQPSender(JmsTemplate defaultJmsTemplate, IJmsTemplateFactory jmsTemplateFactory,
                      ActiveMQQueue docshifterMetricsQueue, int queueReplyTimeout) {
		this.defaultJmsTemplate = defaultJmsTemplate;
		this.jmsTemplateFactory = jmsTemplateFactory;
		this.docshifterMetricsQueue = docshifterMetricsQueue;
		this.queueReplyTimeout = queueReplyTimeout;
	}

	@Override
	public void sendMetrics(DocShifterMetricsReceiverMessage metricsMessage) {
		defaultJmsTemplate.convertAndSend(docshifterMetricsQueue.getName(), metricsMessage, messagePostProcessor -> {
			log.debug("'jmsTemplate.convertAndSend': metricsMessage.taskId={}",
					metricsMessage::getTaskId);
			return messagePostProcessor;
		});
	}
}
