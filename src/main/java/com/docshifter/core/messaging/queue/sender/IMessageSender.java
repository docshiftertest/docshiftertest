package com.docshifter.core.messaging.queue.sender;

import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.SyncTask;
import com.docshifter.core.task.Task;
import com.docshifter.core.task.VeevaTask;

import javax.jms.JMSException;

public interface IMessageSender {

	/**
	 * 
	 * @param ChainConfiguration the workflow configuration
	 * @param task                 The task that will be converted and sent to be
	 *                             processed.
	 */
	void sendTask(ChainConfiguration ChainConfiguration, Task task);

	/**
	 *
	 * @param ChainConfiguration the workflow configuration
	 * @param task                 The task that will be converted and sent to be
	 *                             processed.
	 */
	void sendDocumentumTask(ChainConfiguration ChainConfiguration, DctmTask task);

	/**
	 *
	 * @param ChainConfiguration the workflow configuration
	 * @param task                 The VeevaTask that will be converted and sent to be
	 *                             processed.
	 */
	void sendVeevaTask(ChainConfiguration ChainConfiguration, VeevaTask task);

	void sendPrintTask(ChainConfiguration ChainConfiguration,Task task);

	int getMessageCount() throws JMSException;

	SyncTask sendSyncTask(ChainConfiguration ChainConfiguration, Task task);

}
