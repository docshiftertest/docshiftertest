package com.docshifter.core.messaging.queue.sender;

import com.docbyte.docshifter.config.Task;

public interface IMessageSender extends IMessageSenderOrPublisher {

	void sendTask(long ChainConfigurationID, Task task) throws Exception;

	void sendDocumentumTask(long ChainConfigurationID, Task task) throws Exception;

	void sendPrintTask(Task task) throws Exception;

	void sendTask(String queueName, Task task) throws Exception;

}