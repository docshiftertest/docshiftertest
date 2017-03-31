package com.docshifter.core.messaging.queue.sender;

import com.docshifter.core.task.Task;

public interface IMessageSender extends IMessageSenderOrPublisher {

	void sendTask(long ChainConfigurationID, Task task) ;

	void sendDocumentumTask(long ChainConfigurationID, Task task) ;

	void sendPrintTask(Task task) ;

	void sendTask(String queueName, Task task) ;

	void sendTask(long ChainConfigurationID, Task task, int priority) ;

	void sendDocumentumTask(long ChainConfigurationID, Task task, int priority) ;

	void sendPrintTask(Task task, int priority) ;

	void sendTask(String queueName, Task task, int priority) ;

	int getMessageCount();

}