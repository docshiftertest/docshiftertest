package com.docshifter.core.messaging.sender;

import com.docshifter.core.config.repositories.QueueMonitorRepository;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.messaging.message.DocshifterMessageType;
import com.docshifter.core.messaging.queue.sender.IMessageSender;
import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.services.MetricService;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.docshifter.core.monitoring.services.NotificationService;
import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.SyncTask;
import com.docshifter.core.task.Task;
import com.docshifter.core.task.VeevaTask;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by michiel.vandriessche@docbyte.com on 5/20/16.
 */

public class AMQPSender implements IMessageSender {

	private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

	private final ActiveMQQueue docshifterQueue;
	private final QueueMonitorRepository queueMonitorRepository;
	private final JmsTemplate jmsTemplate;
	private final JmsMessagingTemplate messagingTemplate;

	public static final int DEFAULT_PRIORITY = 4;
	public static final int HIGHEST_PRIORITY = 9;

	@Autowired
	private MetricService metricService;

	@Autowired // This is necessary for the notificationService to work; but now it does!
	private NotificationService notificationService;
	
	public AMQPSender(JmsTemplate jmsTemplate, JmsMessagingTemplate messagingTemplate, ActiveMQQueue docshifterQueue, QueueMonitorRepository queueMonitorRepository) {
		this.jmsTemplate = jmsTemplate;
		this.messagingTemplate = messagingTemplate;
		this.docshifterQueue = docshifterQueue;
		this.queueMonitorRepository = queueMonitorRepository;
	}


	private SyncTask sendSyncTask(String queue, long chainConfigurationID, Task task) {

		Object response = sendTask(DocshifterMessageType.SYNC, queue, chainConfigurationID, task, HIGHEST_PRIORITY);

		if (response == null) {
			//TODO: update to good exception message
			throw new IllegalArgumentException("response object is null");
		}
		
		if (!(response instanceof DocshifterMessage)) {
			//TODO: update to good exception message
			throw new IllegalArgumentException("Return is not of the type 'DocshifterMessage', effective type is: " + response.getClass().getSimpleName());
		}
		
		DocshifterMessage message = (DocshifterMessage) response;
		
		if (message.getType() != DocshifterMessageType.RETURN) {
			//TODO: update to good exception message
			throw new IllegalArgumentException("Message type not supported: " + message.getType());
		}
		
		if (message.getTask() == null) {
			throw new IllegalArgumentException("Message is returnMessage but task is not a SyncTask, it is NULL!!");
		}
		if (message.getTask() instanceof  SyncTask) {
			return (SyncTask) message.getTask();
		} else {
			throw new IllegalArgumentException("Message is returnMessage but task is not a SyncTask, task class is of class: " + task.getClass().getSimpleName());
		}
	}
	
	private Object sendTask(DocshifterMessageType type, String queue, long chainConfigurationID, Task task, int priority) {
		DocshifterMessage message = new DocshifterMessage(
				type,
				task,
				chainConfigurationID);
		
		if (task == null) {
			logger.debug("task=NULL ERROR", null);
		}
		else {
			logger.debug("task.Id=" + task.getId());
			logger.debug("task.class=" + task.getClass().getSimpleName());
			logger.debug("message.task.class=" + message.getTask().getClass().getSimpleName());
		}
		logger.debug("type=" + type.name());
		logger.debug("chainConfigID=" + chainConfigurationID, null);

			/*
			* Calls metricService to handle the basic counts TODO: Handling other metrics as well
			* Sends a notification (best used with database notifications) with the taskID and number of counts
			*  */
			DocumentCounterDTO metrics = metricService.createMetricDto(task.getSourceFilePath());
			String notification = "Notification from sender: Processing " + metrics.getCounts() + " files.";
			notificationService.sendNotification(chainConfigurationID, NotificationLevels.INFO, task.getId(), notification, task.getSourceFilePath(), new File[]{});

		logger.info("Sending message: " + message.toString() + " for file: " + task.getSourceFilePath(), null);
		String hostname = "localhost";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException uncle) {
			logger.warn("Couldn't get hostname of the DocShifter machine, so will default to localhost (for queue_monitor)");
		}
		//QueueMonitor qMon = new QueueMonitor(type.name(), queue, chainConfigurationID, task.getId(), task.getSourceFilePath(), priority, hostname);
		//queueMonitorRepository.save(qMon);

		if (DocshifterMessageType.SYNC.equals(type)) {
			Object obj = messagingTemplate.convertSendAndReceive(queue,message,DocshifterMessage.class, messagePostProcessor -> {
				messagingTemplate.getJmsTemplate().setPriority(HIGHEST_PRIORITY);
				logger.debug("'messagingTemplate.convertSendAndReceive': message.task type=" + message.getTask().getClass().getSimpleName());
				return messagePostProcessor;
			});
			if (obj != null) {
				logger.debug("return on jms 'convertSendAndReceive': obj type" + obj.getClass().getSimpleName());
			} else {
				logger.debug("return on jms 'convertSendAndReceive': obj is null");
			}
			return obj;
		} else {
			jmsTemplate.convertAndSend(queue, message, messagePostProcessor -> {
				jmsTemplate.setPriority(priority);
				logger.debug("'jmsTemplate.convertAndSend': message.task type=" + message.getTask().getClass().getSimpleName());
				return messagePostProcessor;
			});
			return null;
		}
	}

  @Override
  public int getMessageCount() {
	  
	  return this.jmsTemplate.browse(docshifterQueue.getName(), (session, browser) -> {
		  int counter = 0;
		  while (browser.getEnumeration().hasMoreElements()) {
			  counter += 1;
		  }
		  return counter;
	  });
	}

	@Override
	public void sendTask(long chainConfigurationID, Task task, int priority)  {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), chainConfigurationID, task, priority);
	}

	@Override
	public void sendDocumentumTask(long chainConfigurationID, DctmTask task, int priority)  {
		sendTask(DocshifterMessageType.DCTM, docshifterQueue.getName(), chainConfigurationID, task, priority);
	}

	@Override
	public void sendVeevaTask(long chainConfigurationID,VeevaTask task, int priority) {
		sendTask(DocshifterMessageType.VEEVA, docshifterQueue.getName(), chainConfigurationID, task, priority);
	}

	@Override
	public void sendPrintTask(Task task, int priority)  {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), 0, task, priority);
	}
	
	@Override
	public SyncTask sendSyncTask(long chainConfigurationID, Task task) {
		return sendSyncTask(docshifterQueue.getName(), chainConfigurationID, task);
	}
}
