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
import org.apache.log4j.Logger;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Created by michiel.vandriessche@docbyte.com on 5/20/16.
 */

public class AMQPSender implements IMessageSender {

	private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

	private RabbitTemplate rabbitTemplate;
	private Queue docshifterQueue;
	private QueueMonitorRepository queueMonitorRepository;

	public static final int DEFAULT_PRIORITY= 2;
	
	public static final int SYNC_PRIORITY= 4;

	public AMQPSender(RabbitTemplate rabbitTemplate, Queue docshifterQueue, QueueMonitorRepository queueMonitorRepository) {
		this.rabbitTemplate = rabbitTemplate;
		this.docshifterQueue = docshifterQueue;
		this.queueMonitorRepository = queueMonitorRepository;
	}
	
	
	private SyncTask sendSyncTask(DocshifterMessageType type, String queue, long chainConfigurationID, Task task) {
		Object response = sendTask(type, queue, chainConfigurationID, task, SYNC_PRIORITY);

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
		//QueueMonitor qMon = new QueueMonitor(type.name(), queue, chainConfigurationID, task.getId(), task.getSourceFilePath(), priority, hostname);
		//queueMonitorRepository.save(qMon);

		if (DocshifterMessageType.SYNC.equals(type)) {
			Object obj = rabbitTemplate.convertSendAndReceive(queue, message, message1 -> {
				logger.debug("'rabbitTemplate.convertSendAndReceive': message.task type=" + message.getTask().getClass().getSimpleName());
				message1.getMessageProperties().setPriority(SYNC_PRIORITY);
				return message1;
			});
			if (obj != null) {
				logger.debug("return on rabbit 'convertSendAndReceive': obj type" + obj.getClass().getSimpleName());
			} else {
				logger.debug("return on rabbit 'convertSendAndReceive': obj is null");
			}
			return obj;
		} else {
			rabbitTemplate.convertAndSend(queue, message, message1 -> {
				message1.getMessageProperties().setPriority(priority);
				logger.debug("'rabbitTemplate.convertAndSend': message.task type=" + message.getTask().getClass().getSimpleName());
				return message1;
			});
			return null;
		}
	}

	public int getMessageCount(){
		RabbitAdmin rabbitAdmin=new RabbitAdmin(rabbitTemplate.getConnectionFactory());
		Properties props = rabbitAdmin.getQueueProperties(docshifterQueue.getName());
		int messageCount = Integer.parseInt(props.get("QUEUE_MESSAGE_COUNT").toString());
		logger.debug(docshifterQueue.getName() + " has " + messageCount + " messages", null);
		return messageCount;
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
		return sendSyncTask(DocshifterMessageType.SYNC, docshifterQueue.getName(), chainConfigurationID, task);
	}
	
	@Override
	@Deprecated
	public void close() {
		//No longer necessary done by Spring
	}

	@Override
	@Deprecated
	public void run() {
		//No longer necessary done by Spring
	}
}
