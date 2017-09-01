package com.docshifter.core.messaging.sender;

import com.docshifter.core.TestController;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.messaging.message.DocshifterMessageType;
import com.docshifter.core.task.Task;
import com.docshifter.core.work.WorkFolder;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.ReceiveAndReplyCallback;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Paths;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestController.class)
public class AMQPSenderTest {
	
	
	AMQPSender sender;
	
	@Autowired
	Queue defaultQueue;
	
	@Autowired
	RabbitTemplate template;
	
	
	@Before
	public void setUp() throws Exception {
		sender = new AMQPSender(template, defaultQueue);
	}
	
	@Test
	public void sendTask() throws Exception {
	
	}
	
	@Test
	public void sendDocumentumTask() throws Exception {
	
	}
	
//	@Test
//	public void sendSyncTask() throws Exception {
//
//		template.receiveAndReply(defaultQueue.getName(), (ReceiveAndReplyCallback<DocshifterMessage, DocshifterMessage>) docshifterMessage -> {
//			System.out.println("received message");
//			System.out.println(docshifterMessage.getType());
//			return new DocshifterMessage(DocshifterMessageType.RETURN, new Task(), docshifterMessage.getConfigId());
//		});
//		template.convertSendAndReceive(defaultQueue.getName(), new DocshifterMessage(DocshifterMessageType.DCTM, null, 1l));
//
//
//		Task task =
//				new Task(Paths.get("target/test-classes/ds/work/.empty"),
//						new WorkFolder(
//								Paths.get("target/test-classes/ds/work"),
//								Paths.get("target/test-classes/ds/error")
//						)
//				);
//
//
//		Task returntask = sender.sendSyncTask(1, task);
//
//		assertNotNull(returntask);
//
//	}
	
	
}