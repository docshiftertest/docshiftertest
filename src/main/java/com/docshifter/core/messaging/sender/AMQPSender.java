package com.docshifter.core.messaging.sender;

import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.messaging.message.DocshifterMessageType;
import com.docshifter.core.messaging.queue.sender.IMessageSender;
import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.Task;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Properties;

/**
 * Created by michiel.vandriessche@docbyte.com on 5/20/16.
 */

public class AMQPSender implements IMessageSender {

	private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

	private RabbitTemplate rabbitTemplate;
	private Queue docshifterQueue;

	private static final int DEFAULT_PRIORITY= 2;

	public AMQPSender(RabbitTemplate rabbitTemplate, Queue docshifterQueue) {
		this.rabbitTemplate = rabbitTemplate;
		this.docshifterQueue = docshifterQueue;
	}


	private void sendTask(DocshifterMessageType type, String queue, long chainConfigurationID, Task task, int priority)  {
		DocshifterMessage message=new DocshifterMessage(
				type,
				task,
				chainConfigurationID);

		logger.debug("type=" + type.name(), null);
		logger.debug("task=" + task.getId(), null);

		if (task == null){
			logger.debug("task=NULL ERROR", null);
		}
		logger.debug("chainConfigID=" + chainConfigurationID, null);

		logger.info("Sending message: " + message.toString()+" for file: "+task.getSourceFilePath(),null);

		rabbitTemplate.convertAndSend(queue, message, message1 -> {
			message1.getMessageProperties().setPriority(priority);
			return message1;
		});
	}

	private void sendTask(DocshifterMessageType type, long chainConfigurationID, Task task, int priority)  {
		sendTask(type, docshifterQueue.getName(), chainConfigurationID, task, priority);
	}

	private void sendTask(DocshifterMessageType type, Task task, int priority)  {
		sendTask(type, docshifterQueue.getName(), 0, task, priority);
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
	public void sendPrintTask(Task task, int priority)  {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), 0, task, priority);
	}

	@Override
	public void sendTask(String queueName, Task task, int priority)  {
		sendTask(DocshifterMessageType.DEFAULT, queueName, 0, task, priority);
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
