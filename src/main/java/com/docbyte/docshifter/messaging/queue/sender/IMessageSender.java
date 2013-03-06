package com.docbyte.docshifter.messaging.queue.sender;

import javax.jms.JMSException;

import com.docbyte.docshifter.config.Task;
import com.docbyte.docshifter.messaging.IMessageSenderOrPublisher;

public interface IMessageSender extends IMessageSenderOrPublisher  {
	
	abstract public void sendTask(long ChainConfigurationID,Task task) throws JMSException;
	abstract public void sendDocumentumTask(long ChainConfigurationID, Task task) throws JMSException;
	public abstract void sendPrintTask(Task task) throws JMSException;
	public abstract void sendTask(String queueName, Task task)
			throws JMSException;
	
}