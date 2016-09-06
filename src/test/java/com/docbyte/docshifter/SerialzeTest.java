package com.docbyte.docshifter;

import com.docbyte.docshifter.config.Task;
import com.docbyte.docshifter.messaging.factory.IConnectionFactory;
import com.docbyte.docshifter.messaging.factory.MessagingConnectionFactory;
import com.docshifter.core.work.WorkFolder;
import org.junit.Before;
import org.junit.Test;

import javax.jms.*;
import java.nio.file.Paths;

/**
 * Created by samnang.nop on 3/03/2016.
 */


public class SerialzeTest {

	public Connection connection;
	public Session session;
	private Destination destination;
	private MessageProducer producer;

	@Test
	public void serWF() throws Exception {

		WorkFolder wf = new WorkFolder(Paths.get("C:\\TEST"), null);
		Task task = new Task("test",wf);

		System.out.print(Paths.get("C:\\TEST").getClass());

		ObjectMessage om = session.createObjectMessage(task);
		producer.send(om);

	}

	@Before
	public void init() throws Exception{


			IConnectionFactory connectionFactory = MessagingConnectionFactory.getConnectionFactory("admin", "admin", "localhost:7676", "OPENMQ");
			connection = connectionFactory.createConnection();
			connection.start();


				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);


			destination = session.createQueue("test");
			producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);



	}

}
