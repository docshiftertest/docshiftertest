package com.docshifter.core.messaging.sender;

import com.docshifter.core.messaging.queue.sender.IMessageSender;
import com.docbyte.docshifter.util.Logger;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.messaging.message.DocshifterMessageType;
import com.docshifter.core.task.Task;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by michiel.vandriessche@docbyte.com on 5/20/16.
 */

public class AMQPSender implements IMessageSender {

	private RabbitTemplate rabbitTemplate;
	private Queue docshifterQueue;

	public AMQPSender(RabbitTemplate rabbitTemplate, Queue docshifterQueue) {
		this.rabbitTemplate = rabbitTemplate;
		this.docshifterQueue = docshifterQueue;
	}

	private void sendTask(DocshifterMessageType type, String queue, long chainConfigurationID, Task task) throws Exception {
		DocshifterMessage message=new DocshifterMessage(
				type,
				task,
				chainConfigurationID);

		Logger.info("Sending message: " + message.toString()+" for file: "+task.getSourceFilePath(),null);

		rabbitTemplate.convertAndSend(queue, message);
	}

	private void sendTask(DocshifterMessageType type, long chainConfigurationID, Task task) throws Exception {
		sendTask(type, docshifterQueue.getName(), chainConfigurationID, task);
	}

	private void sendTask(DocshifterMessageType type, Task task) throws Exception {
		sendTask(type, docshifterQueue.getName(), 0, task);
	}

	@Override
	public void sendTask(long chainConfigurationID, Task task) throws Exception {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), chainConfigurationID, task);
	}

	@Override
	public void sendDocumentumTask(long chainConfigurationID, Task task) throws Exception {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), chainConfigurationID, task);
	}

	@Override
	public void sendPrintTask(Task task) throws Exception {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), 0, task);
	}

	@Override
	public void sendTask(String queueName, Task task) throws Exception {
		sendTask(DocshifterMessageType.DEFAULT, queueName, 0, task);
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
