package com.docshifter.core.messaging.sender;

import com.docshifter.core.config.domain.QueueMonitor;
import com.docshifter.core.config.domain.QueueMonitorRepository;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.messaging.message.DocshifterMessageType;
import com.docshifter.core.messaging.queue.sender.IMessageSender;
import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.SyncTask;
import com.docshifter.core.task.Task;
import com.docshifter.core.task.VeevaTask;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.jms.JMSException;

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

		logger.info("Sending message: " + message.toString() + " for file: " + task.getSourceFilePath(), null);
		String hostname = "localhost";
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException uncle) {
			logger.warn("Couldn't get hostname of the DocShifter machine, so will default to localhost (for queue_monitor)");
		}
		QueueMonitor qMon = new QueueMonitor(type.name(), queue, chainConfigurationID, task.getId(), task.getSourceFilePath(), priority, hostname);
		queueMonitorRepository.save(qMon);

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

  public int getMessageCount() throws JMSException {
		/*
		 * RabbitAdmin rabbitAdmin=new
		 * RabbitAdmin(rabbitTemplate.getConnectionFactory()); Properties props =
		 * rabbitAdmin.getQueueProperties(docshifterQueue.getName()); int messageCount =
		 * Integer.parseInt(props.get("QUEUE_MESSAGE_COUNT").toString());
		 * logger.debug(docshifterQueue.getName() + " has " + messageCount +
		 * " messages", null);
		 */
		return 1;
	}

	@Override
	public void sendTask(long chainConfigurationID, Task task)  {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), chainConfigurationID, task, DEFAULT_PRIORITY);
	}

	@Override
	public void sendDocumentumTask(long chainConfigurationID, DctmTask task)  {
		sendTask(DocshifterMessageType.DCTM, docshifterQueue.getName(), chainConfigurationID, task, DEFAULT_PRIORITY);
	}

	@Override
	public void sendPrintTask(Task task)  {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), 0, task, DEFAULT_PRIORITY);
	}

	@Override
	public void sendTask(String queueName, Task task)  {
		sendTask(DocshifterMessageType.DEFAULT, queueName, 0, task, DEFAULT_PRIORITY);
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
	public void sendVeevaTask(long chainConfigurationID,VeevaTask task) {
		sendTask(DocshifterMessageType.VEEVA, docshifterQueue.getName(), chainConfigurationID, task, DEFAULT_PRIORITY);
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
	public void sendTask(String queueName, Task task, int priority)  {
		sendTask(DocshifterMessageType.DEFAULT, queueName, 0, task, priority);
	}
	
	@Override
	public SyncTask sendSyncTask(long chainConfigurationID, Task task) {
		return sendSyncTask(docshifterQueue.getName(), chainConfigurationID, task);
	}
}