package com.docbyte.docshifter.messaging.queue.sender;

import com.docbyte.docshifter.config.Task;
import com.docbyte.docshifter.messaging.IMessageSenderOrPublisher;

public interface IMessageSender extends IMessageSenderOrPublisher {
	
	void sendTask(long ChainConfigurationID,Task task) throws Exception;
	void sendDocumentumTask(long ChainConfigurationID, Task task) throws Exception;
	void sendPrintTask(Task task) throws Exception;
	void sendTask(String queueName, Task task) throws Exception;
	
}