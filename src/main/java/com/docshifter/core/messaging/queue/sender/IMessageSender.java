package com.docshifter.core.messaging.queue.sender;

import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.SyncTask;
import com.docshifter.core.task.Task;
import com.docshifter.core.task.VeevaTask;

public interface IMessageSender extends IMessageSenderOrPublisher {

	void sendTask(long ChainConfigurationID, Task task) ;
	
	void sendTask(long ChainConfigurationID, Task task, int priority) ;
	
	void sendDocumentumTask(long ChainConfigurationID, DctmTask task) ;
	
	void sendDocumentumTask(long ChainConfigurationID, DctmTask task, int priority) ;

	void sendVeevaTask(long ChainConfigurationID, VeevaTask task);

	void sendVeevaTask(long ChainConfigurationID,VeevaTask task, int priority);
	
	void sendPrintTask(Task task) ;

	void sendTask(String queueName, Task task) ;

	void sendPrintTask(Task task, int priority) ;

	void sendTask(String queueName, Task task, int priority) ;

	int getMessageCount();
	
	SyncTask sendSyncTask(long ChainConfigurationID, Task task) ;

}