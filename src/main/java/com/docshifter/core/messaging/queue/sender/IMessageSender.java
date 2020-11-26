package com.docshifter.core.messaging.queue.sender;

import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.SyncTask;
import com.docshifter.core.task.Task;
import com.docshifter.core.task.VeevaTask;

import javax.jms.JMSException;

public interface IMessageSender {

	void sendTask(long ChainConfigurationID, Task task);

	/**
	 * 
	 * @param ChainConfigurationID the workflow configurationID
	 * @param task                 The task that will be converted and sent to be
	 *                             processed.
	 * @param priority             Default (JMSPriority == 4) High (JMSPriority > 4
	 *                             && <= 9) Low (JMSPriority > 0 && < 4)
	 */
	void sendTask(long ChainConfigurationID, Task task, int priority);

	void sendDocumentumTask(long ChainConfigurationID, DctmTask task);

	/**
	 * 
	 * @param ChainConfigurationID the workflow configurationID
	 * @param task                 Documentum Task
	 * @param priority             Default (JMSPriority == 4) High (JMSPriority > 4
	 *                             && <= 9) Low (JMSPriority > 0 && < 4)
	 */
	void sendDocumentumTask(long ChainConfigurationID, DctmTask task, int priority);

	void sendVeevaTask(long ChainConfigurationID, VeevaTask task);

	/**
	 * 
	 * @param ChainConfigurationID the workflow configurationID
	 * @param task                 The Veeva task that will be converted and sent to
	 *                             be processed.
	 * @param priority             Default (JMSPriority == 4) High (JMSPriority > 4
	 *                             && <= 9) Low (JMSPriority > 0 && < 4)
	 */
	void sendVeevaTask(long ChainConfigurationID, VeevaTask task, int priority);

	void sendPrintTask(Task task);

	void sendTask(String queueName, Task task);

	void sendPrintTask(Task task, int priority);

	void sendTask(String queueName, Task task, int priority);

	int getMessageCount() throws JMSException;

	SyncTask sendSyncTask(long ChainConfigurationID, Task task);

}