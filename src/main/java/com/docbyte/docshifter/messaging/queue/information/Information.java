package com.docbyte.docshifter.messaging.queue.information;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import com.docbyte.docshifter.messaging.factory.IConnectionFactory;
import com.docbyte.docshifter.messaging.factory.MessagingConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import java.util.Enumeration;

public class Information 
{
	private String user;
	private String password;
	private String url;
	private String queueName;
	private IConnectionFactory connectionFactory;

	public Information() {
		GeneralConfigurationBean config = ConfigurationServer.getGeneralConfiguration();
		user = config.getString(Constants.MQ_USER);
		password = config.getString(Constants.MQ_PASSWORD);
		url = config.getString(Constants.MQ_URL);
		queueName = config.getString(Constants.MQ_QUEUE);
		try {
			connectionFactory = MessagingConnectionFactory.getConnectionFactory(user, password, url);
		} catch (JMSException e) {
			e.printStackTrace();
		}



	}

	
	
	public int getNumberOfMessages()
	{
		int count = 0;


		try (Connection connection = connectionFactory.createConnection()) {

			connection.start();


			try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

				try (QueueBrowser browser = session.createBrowser(session.createQueue(queueName))) {

					Enumeration enumeration = browser.getEnumeration();

					while (enumeration.hasMoreElements()) {
						enumeration.nextElement();


						//Logger.info(message.toString(),null);
						count++;
					}
				} catch (JMSException e) {
					e.printStackTrace();
					return -1;
				}
			} catch (JMSException e) {
				e.printStackTrace();
				return -1;
			}
		}  catch (JMSException e) {
			e.printStackTrace();
			return -1;
		}


			
		//RETURN MESSAGE COUNT DIVIDED BY 2 BECAUSE THERE IS A MESSAGE FROM THE QUEUE AND A MESSAGE FROM THE
		return count;
	}

}
