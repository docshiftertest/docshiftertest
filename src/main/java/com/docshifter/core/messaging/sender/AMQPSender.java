package com.docshifter.core.messaging.sender;

import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.repositories.QueueMonitorRepository;
import com.docshifter.core.config.services.IJmsTemplateFactory;
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

import java.util.concurrent.TimeUnit;

/**
 * Created by michiel.vandriessche@docbyte.com on 5/20/16.
 */

@Log4j2
public class AMQPSender implements IMessageSender {

	private final ActiveMQQueue docshifterQueue;
	private final QueueMonitorRepository queueMonitorRepository;
	private final JmsTemplate defaultJmsTemplate;
	private final IJmsTemplateFactory jmsTemplateFactory;
	
	public AMQPSender(JmsTemplate defaultJmsTemplate, IJmsTemplateFactory jmsTemplateFactory,
					  ActiveMQQueue docshifterQueue,
					  QueueMonitorRepository queueMonitorRepository) {
		this.defaultJmsTemplate = defaultJmsTemplate;
		this.jmsTemplateFactory = jmsTemplateFactory;
		this.docshifterQueue = docshifterQueue;
		this.queueMonitorRepository = queueMonitorRepository;
	}

	private SyncTask sendSyncTask(String queue, ChainConfiguration chainConfiguration, Task task) {

		Object response = sendTask(DocshifterMessageType.SYNC, queue, chainConfiguration, task);

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
	
	private Object sendTask(DocshifterMessageType type, String queue, ChainConfiguration chainConfiguration, Task task) {
		DocshifterMessage message = new DocshifterMessage(
				type,
				task,
				chainConfiguration.getId());
		
		if (task == null) {
			log.debug("task=NULL ERROR");
		}

		else {
			log.debug("task.Id={}", task.getId());
			log.debug("task.class={}", task.getClass().getSimpleName());
			log.debug("message.task.class={}", message.getTask().getClass().getSimpleName());
		}
		log.debug("type={}", type.name());
		log.debug("chainConfigID={}", chainConfiguration.getId());

		// Try to get the priority from webservice request otherwise uses workflow priority
		int taskPriority = (int) task.getData().getOrDefault("priority", chainConfiguration.getPriority());

		// Gets the sync webservices workflow timeout from webservice request
		Integer wsTimeout = (Integer) task.getData().get("timeout");

		long taskTimeoutInSeconds = wsTimeout != null ? (long) wsTimeout : chainConfiguration.getTimeout();

		long taskTimeoutInMillis = TimeUnit.SECONDS.toMillis(taskTimeoutInSeconds);

		log.info("Sending message: {} for file: {} using workflow {} ", message, task.getSourceFilePath(), chainConfiguration.getName());

		JmsTemplate jmsTemplate = jmsTemplateFactory.create(taskPriority, taskTimeoutInMillis);
		if (DocshifterMessageType.SYNC.equals(type)) {
			JmsMessagingTemplate messagingTemplate = new JmsMessagingTemplate(jmsTemplate);
			Object obj = messagingTemplate.convertSendAndReceive(queue, message, DocshifterMessage.class, messagePostProcessor -> {
				log.debug("'messagingTemplate.convertSendAndReceive': message.task type=" + message.getTask().getClass().getSimpleName());
				return messagePostProcessor;
			});
			if (obj != null) {
				log.debug("return on jms 'convertSendAndReceive': obj type {}", obj.getClass().getSimpleName());
			}
			else {
				log.debug("return on jms 'convertSendAndReceive': obj is null");
			}
			return obj;
		}
		else {
			jmsTemplate.convertAndSend(queue, message, messagePostProcessor -> {
				log.debug("'jmsTemplate.convertAndSend': message.task type={}", message.getTask().getClass().getSimpleName());
				return messagePostProcessor;
			});
			return null;
		}
	}

  @Override
  public int getMessageCount() {
	  
	  return this.defaultJmsTemplate.browse(docshifterQueue.getName(), (session, browser) -> {
		  int counter = 0;
		  while (browser.getEnumeration().hasMoreElements()) {
			  counter += 1;
		  }
		  return counter;
	  });
	}

	@Override
	public void sendTask(ChainConfiguration chainConfiguration, Task task)  {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), chainConfiguration, task);
	}

	@Override
	public void sendDocumentumTask(ChainConfiguration chainConfiguration, DctmTask task)  {
		sendTask(DocshifterMessageType.DCTM, docshifterQueue.getName(), chainConfiguration, task);
	}

	@Override
	public void sendVeevaTask(ChainConfiguration chainConfiguration,VeevaTask task) {
		sendTask(DocshifterMessageType.VEEVA, docshifterQueue.getName(), chainConfiguration, task);
	}

	@Override
	public void sendPrintTask(ChainConfiguration chainConfiguration,Task task)  {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), chainConfiguration, task);
	}
	
	@Override
	public SyncTask sendSyncTask(ChainConfiguration chainConfiguration, Task task) {
		return sendSyncTask(docshifterQueue.getName(), chainConfiguration, task);
	}
}
