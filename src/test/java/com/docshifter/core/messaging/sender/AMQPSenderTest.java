package com.docshifter.core.messaging.sender;

import com.docshifter.core.TestController;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.task.Task;
import com.docshifter.core.work.WorkFolder;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Paths;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestController.class)
public class AMQPSenderTest {
	
	private Logger log = Logger.getLogger(AMQPSenderTest.class);
	
	AMQPSender sender;
	
	@Autowired
	Queue defaultQueue;
	
	@Autowired
	RabbitTemplate template;
	
	
	@Autowired
	private AmqpAdmin amqpAdmin;
	
	
	@Before
	public void setUp() throws Exception {
		sender = new AMQPSender(template, defaultQueue);
		amqpAdmin.purgeQueue(defaultQueue.getName(), false);
	}
	
	@Test
	public void sendTask() throws Exception {
		Task task = new Task(Paths.get("target/test-classes/ds/work/.empty"),
				new WorkFolder(
						Paths.get("target/test-classes/ds/work"),
						Paths.get("target/test-classes/ds/error")
				));
		Date test = new Date();
		
		
		task.getData().put("testDate", test);
		
		sender.sendTask(defaultQueue.getName(), task);
		
		DocshifterMessage response = (DocshifterMessage) template.receiveAndConvert(defaultQueue.getName());
		log.info(response.toString());
		assertTrue(response.getTask().getData().get("testDate") instanceof Date);
		assertEquals(test.toString(), ((Date) response.getTask().getData().get("testDate")).toString());
	}
	
	@Test
	public void sendDocumentumTask() throws Exception {
	
	}
	
	/**
	 * this throws IllegalArgumentException becaus there is no return response within timeout => return object is null
	 *
	 * @throws IllegalArgumentException
	 */
	//@Test(expected = IllegalArgumentException.class)
	@Test
	public void sendSyncTask() throws Exception {

		/*template.receiveAndReply(defaultQueue.getName(), (ReceiveAndReplyCallback<DocshifterMessage, DocshifterMessage>) docshifterMessage -> {
			System.out.println("received message");
			System.out.println(docshifterMessage.getType());
			return new DocshifterMessage(DocshifterMessageType.RETURN, new Task(), docshifterMessage.getConfigId());
		});
		template.convertSendAndReceive(defaultQueue.getName(), new DocshifterMessage(DocshifterMessageType.DCTM, null, 1l));


		SyncTask task =
				new SyncTask(Paths.get("target/test-classes/ds/work/.empty"),
						new WorkFolder(
								Paths.get("target/test-classes/ds/work"),
								Paths.get("target/test-classes/ds/error")
						), true
				);


		SyncTask returntask = sender.sendSyncTask(1, task);

		assertNotNull(returntask);*/
		
		//TODO: fixe test setup
		//https://dzone.com/articles/mocking-rabbitmq-for-integration-tests
		
	}
	
}