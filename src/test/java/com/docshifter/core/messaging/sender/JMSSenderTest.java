package com.docshifter.core.messaging.sender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Date;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import com.docshifter.core.config.domain.QueueMonitorRepository;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.task.Task;
import com.docshifter.core.work.WorkFolder;

@SpringBootTest
@EnableJms
public class JMSSenderTest {

	private Logger log = Logger.getLogger(JMSSenderTest.class);

	AMQPSender sender;

	@Autowired
	private QueueMonitorRepository queueMonitorRepository;

	@Autowired
	Queue defaultQueue;

	@Autowired
	public JmsTemplate template;


	@Before
	public void setUp() throws Exception {
		sender = new AMQPSender(template, defaultQueue, queueMonitorRepository);
	}

	@Test
	public void sendTask() throws Exception {
		Task task = new Task(Paths.get("target/test-classes/ds/work/.empty").toString(),
				new WorkFolder(Paths.get("target/test-classes/ds/work"), Paths.get("target/test-classes/ds/error")));
		Date test = new Date();

		task.getData().put("testDate", test);

		sender.sendTask(defaultQueue.getName(), task);

		DocshifterMessage response = (DocshifterMessage) template.receiveAndConvert(defaultQueue.getName());
		log.info(response.toString());
		assertTrue(response.getTask().getData().get("testDate") instanceof Date);
		assertEquals(test.toString(), ((Date) response.getTask().getData().get("testDate")).toString());
	}
	
    @Value("${spring.artemis.host}")
    private String brokerUrl;

    @Bean
    public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
      return new ActiveMQConnectionFactory(brokerUrl);
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
      return new CachingConnectionFactory(
          senderActiveMQConnectionFactory());
    }

    @Bean
    public JmsTemplate jmsTemplate() {
      return new JmsTemplate(cachingConnectionFactory());
    }

}
