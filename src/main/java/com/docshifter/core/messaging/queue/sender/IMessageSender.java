package com.docshifter.core.messaging.queue.sender;

import com.docshifter.core.task.Task;

public interface IMessageSender extends IMessageSenderOrPublisher {

	void sendTask(long ChainConfigurationID, Task task) throws Exception;

	void sendDocumentumTask(long ChainConfigurationID, Task task) throws Exception;

	void sendPrintTask(Task task) throws Exception;

	void sendTask(String queueName, Task task) throws Exception;

	void sendTask(long ChainConfigurationID, Task task, int priority) throws Exception;

	void sendDocumentumTask(long ChainConfigurationID, Task task, int priority) throws Exception;

	void sendPrintTask(Task task, int priority) throws Exception;

	void sendTask(String queueName, Task task, int priority) throws Exception;

	int getMessageCount();

}