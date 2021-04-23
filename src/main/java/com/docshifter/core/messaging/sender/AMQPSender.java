package com.docshifter.core.messaging.sender;

import com.docshifter.core.config.repositories.QueueMonitorRepository;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.messaging.message.DocshifterMessageType;
import com.docshifter.core.messaging.queue.sender.IMessageSender;
import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.SyncTask;
import com.docshifter.core.task.Task;
import com.docshifter.core.task.VeevaTask;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.JmsDestinationAccessor;

import java.util.concurrent.TimeUnit;

/**
 * Created by michiel.vandriessche@docbyte.com on 5/20/16.
 */

@Log4j2
public class AMQPSender implements IMessageSender {

	private final ActiveMQQueue docshifterQueue;
	//private final QueueMonitorRepository queueMonitorRepository;
	private final JmsTemplate jmsTemplate;
	private final JmsMessagingTemplate messagingTemplate;

	public static final int DEFAULT_PRIORITY = 4;
	public static final int HIGHEST_PRIORITY = 9;
	
	public AMQPSender(JmsTemplate jmsTemplate, JmsMessagingTemplate messagingTemplate, ActiveMQQueue docshifterQueue) {
		this.jmsTemplate = jmsTemplate;
		this.messagingTemplate = messagingTemplate;
		this.docshifterQueue = docshifterQueue;
		//this.queueMonitorRepository = queueMonitorRepository;
	}


	private SyncTask sendSyncTask(String queue, long chainConfigurationID, long timeout, Task task) {

		Object response = sendTask(DocshifterMessageType.SYNC, queue, chainConfigurationID, task, HIGHEST_PRIORITY,
				timeout);

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
	
	private Object sendTask(DocshifterMessageType type, String queue, long chainConfigurationID, Task task,
							int priority, long timeout) {
		if (task == null) {
			throw new NullPointerException("The task to send cannot be NULL!");
		}

		DocshifterMessage message = new DocshifterMessage(
				type,
				task,
				chainConfigurationID);

		log.debug("task.Id={}", task.getId());
		log.debug("task.class={}", () -> task.getClass().getSimpleName());
		log.debug("message.task.class={}", () -> message.getTask().getClass().getSimpleName());
		log.debug("type={}", type.name());
		log.debug("chainConfigID={}", chainConfigurationID);

		log.info("Sending message: {} for file: {}", message, task.getSourceFilePath());
		// String hostname = "localhost";
		// try {
		// 	hostname = InetAddress.getLocalHost().getHostName();
		// }
		// catch (UnknownHostException uncle) {
		// 	logger.warn("Couldn't get hostname of the DocShifter machine, so will default to localhost (for queue_monitor)");
		// }
		//QueueMonitor qMon = new QueueMonitor(type.name(), queue, chainConfigurationID, task.getId(), task.getSourceFilePath(), priority, hostname);
		//queueMonitorRepository.save(qMon);

		if (DocshifterMessageType.SYNC.equals(type)) {
			Object obj = messagingTemplate.convertSendAndReceive(queue,message,DocshifterMessage.class, messagePostProcessor -> {
				messagingTemplate.getJmsTemplate().setPriority(HIGHEST_PRIORITY);
				long timeoutInMs = timeout > 0 ? TimeUnit.SECONDS.toMillis(timeout) : JmsDestinationAccessor.RECEIVE_TIMEOUT_INDEFINITE_WAIT;
				log.debug("Message receive timeout was {}, setting it to {}",
						messagingTemplate.getJmsTemplate().getReceiveTimeout(), timeoutInMs);
				messagingTemplate.getJmsTemplate().setReceiveTimeout(timeoutInMs);
				log.debug("'messagingTemplate.convertSendAndReceive': message.task type={}",
						() -> message.getTask().getClass().getSimpleName());
				return messagePostProcessor;
			});
			if (obj != null) {
				log.debug("return on jms 'convertSendAndReceive': obj type={}", () -> obj.getClass().getSimpleName());
			} else {
				log.debug("return on jms 'convertSendAndReceive': obj is NULL!");
			}
			return obj;
		} else {
			jmsTemplate.convertAndSend(queue, message, messagePostProcessor -> {
				jmsTemplate.setPriority(priority);
				log.debug("'jmsTemplate.convertAndSend': message.task type={}",
						() -> message.getTask().getClass().getSimpleName());
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
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), chainConfigurationID, task, priority, 0);
	}

	@Override
	public void sendDocumentumTask(long chainConfigurationID, DctmTask task, int priority)  {
		sendTask(DocshifterMessageType.DCTM, docshifterQueue.getName(), chainConfigurationID, task, priority, 0);
	}

	@Override
	public void sendVeevaTask(long chainConfigurationID,VeevaTask task, int priority) {
		sendTask(DocshifterMessageType.VEEVA, docshifterQueue.getName(), chainConfigurationID, task, priority, 0);
	}

	@Override
	public void sendPrintTask(Task task, int priority)  {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), 0, task, priority, 0);
	}
	
	@Override
	public SyncTask sendSyncTask(long chainConfigurationID, long timeout, Task task) {
		return sendSyncTask(docshifterQueue.getName(), chainConfigurationID, timeout, task);
	}
}
