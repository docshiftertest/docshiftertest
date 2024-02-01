package com.docshifter.core.messaging.sender;

import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.services.IJmsTemplateFactory;
import com.docshifter.core.config.services.OngoingTaskService;
import com.docshifter.core.messaging.dto.DocShifterMessageDTO;
import com.docshifter.core.messaging.message.DocShifterMetricsSenderMessage;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.messaging.message.DocshifterMessageType;
import com.docshifter.core.messaging.queue.sender.IMessageSender;
import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.SyncTask;
import com.docshifter.core.task.Task;
import com.docshifter.core.task.TaskStatus;
import com.docshifter.core.task.VeevaTask;
import com.docshifter.core.utils.FileUtils;
import com.docshifter.core.utils.NetworkUtils;
import com.docshifter.core.work.WorkFolder;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Message;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by michiel.vandriessche@docbyte.com on 5/20/16.
 */

@Log4j2
public class AMQPSender implements IMessageSender {

	private final ActiveMQQueue docshifterQueue;
	private final ActiveMQQueue docshifterMetricsQueue;
	private final JmsTemplate defaultJmsTemplate;
	private final JmsTemplate metricsJmsTemplate;
	private final IJmsTemplateFactory jmsTemplateFactory;
	private final int queueReplyTimeout;
	private final OngoingTaskService ongoingTaskService;

	public AMQPSender(JmsTemplate defaultJmsTemplate, JmsTemplate metricsJmsTemplate,
					  IJmsTemplateFactory jmsTemplateFactory,
					  ActiveMQQueue docshifterQueue, ActiveMQQueue docshifterMetricsQueue,
					  int queueReplyTimeout, OngoingTaskService ongoingTaskService) {
		this.defaultJmsTemplate = defaultJmsTemplate;
		this.metricsJmsTemplate = metricsJmsTemplate;
		this.jmsTemplateFactory = jmsTemplateFactory;
		this.docshifterQueue = docshifterQueue;
		this.docshifterMetricsQueue = docshifterMetricsQueue;
		this.queueReplyTimeout = queueReplyTimeout;
		this.ongoingTaskService = ongoingTaskService;
	}

	private SyncTask sendSyncTask(String queue, ChainConfiguration chainConfiguration, Task task) {

		Object response = sendTask(DocshifterMessageType.SYNC, queue, chainConfiguration, task);

		if (response == null) {
			task.setStatus(TaskStatus.TIMED_OUT);
			log.error("Timeout exception: Your task has expired and has been removed from the queue." +
					"You have defined a timeout of {} seconds", task.getData().getOrDefault("timeout",chainConfiguration.getTimeout()));
			return (SyncTask) task;
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
	
	private Object sendTask(DocshifterMessageType type, String queue,
							ChainConfiguration chainConfiguration, Task task) {
		if (task == null) {
			throw new IllegalArgumentException("The task to send cannot be NULL!");
		}

		log.debug("Creating metrics message in Sender...");
		DocShifterMetricsSenderMessage metricsMessage = DocShifterMetricsSenderMessage
				.builder()
				.taskId(task.getId())
				.hostName(NetworkUtils.getLocalHostName())
				.senderPickedUp(System.currentTimeMillis())
				.workflowName(chainConfiguration.getName())
				.documentPathList(
						FileUtils.copySourceFilePathToList(
								task.getSourceFilePath(),
								task.getWorkFolder()
						)
				)
				.workFolder(task.getWorkFolder())
				.build();

		log.debug("...about to send it...");
		sendMetrics(metricsMessage);
		log.debug("...sent!");

		DocshifterMessage message = new DocshifterMessage(
				type,
				task,
				chainConfiguration.getId()
		);

		log.debug("task.Id={}", task.getId());
		log.debug("task.class={}", () -> task.getClass().getSimpleName());
		log.debug("message.task.class={}", () -> message.getTask().getClass().getSimpleName());
		log.debug("type={}", type.name());
		log.debug("chainConfigID={}", chainConfiguration.getId());

		// Try to get the priority from webservice request otherwise uses workflow priority
		int taskPriority = (int) task.getData().getOrDefault("priority", chainConfiguration.getPriority());

		// Use the provided timeout value if explicitly specified on the task data, otherwise use the configured reply timeout
		Integer wsTimeout = (Integer) task.getData().get("timeout");
		long taskTimeoutInMillis;
		if (wsTimeout != null) {
			taskTimeoutInMillis =  TimeUnit.SECONDS.toMillis(wsTimeout);
			//The receiver expects to receive a long value instead of an integer
			task.getData().put("timeout", Long.valueOf(wsTimeout));
		} else {
			taskTimeoutInMillis = queueReplyTimeout;
			if (DocshifterMessageType.SYNC.equals(type)) {
				// On a sync task we only get a reply after task completion, so increase the timeout by the value set
				// on the workflow
				taskTimeoutInMillis += TimeUnit.SECONDS.toMillis(chainConfiguration.getTimeout());
			}
		}

		log.info("Sending message: {} (priority = {}, timeout = {} ms) for file: {} using workflow {}", message,
				taskPriority, taskTimeoutInMillis, task.getSourceFilePath(), chainConfiguration.getName());

		if (DocshifterMessageType.SYNC.equals(type)) {
			JmsTemplate jmsTemplate = jmsTemplateFactory.create(taskPriority, taskTimeoutInMillis, taskTimeoutInMillis);
			JmsMessagingTemplate messagingTemplate = new JmsMessagingTemplate(jmsTemplate);
			Object obj = messagingTemplate.convertSendAndReceive(queue, message, DocshifterMessage.class, messagePostProcessor -> {
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
		}
		else {
			final AtomicReference<Message> msg = new AtomicReference<>();

			JmsTemplate jmsTemplate = jmsTemplateFactory.create(taskPriority, taskTimeoutInMillis, 0);
			jmsTemplate.convertAndSend(queue, message, messagePostProcessor -> {
				log.debug("'jmsTemplate.convertAndSend': message.task type={}",
						() -> message.getTask().getClass().getSimpleName());

				msg.set(messagePostProcessor);

				return messagePostProcessor;
			});

			ongoingTaskService.notifyConsoleOngoingTask(
					(ActiveMQMessage) msg.get(),
					DocShifterMessageDTO.Status.WAITING_TO_BE_PROCESSED
			);

			return null;
		}
	}

	@Override
	public int getMessageCount() {
		return getMessageCount(docshifterQueue.getName());
	}

	@Override
	public int getMetricsMessageCount() {
		  return getMessageCount(docshifterMetricsQueue.getName());
	  }

	private int getMessageCount(String queue) {
		if (this.defaultJmsTemplate == null || StringUtils.isBlank(queue)) {
			return 0;
		}
		else {
			final Integer browse = this.defaultJmsTemplate.browse(queue, (session, browser) -> {
				int counter = 0;
				if (browser == null || browser.getEnumeration() == null) {
					return counter;
				}
				Enumeration enumeration = browser.getEnumeration();
				while (enumeration.hasMoreElements()) {
					enumeration.nextElement();
					counter += 1;
				}
				return counter;
			});
			return browse;
		}
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
	public void sendVeevaTask(ChainConfiguration chainConfiguration, VeevaTask task) {
		sendTask(DocshifterMessageType.VEEVA, docshifterQueue.getName(), chainConfiguration, task);
	}

	@Override
	public void sendPrintTask(ChainConfiguration chainConfiguration, Task task)  {
		sendTask(DocshifterMessageType.DEFAULT, docshifterQueue.getName(), chainConfiguration, task);
	}
	
	@Override
	public SyncTask sendSyncTask(ChainConfiguration chainConfiguration, Task task) {
		return sendSyncTask(docshifterQueue.getName(), chainConfiguration, task);
	}

	@Override
	public void sendMetrics(DocShifterMetricsSenderMessage metricsMessage) {
		metricsJmsTemplate.convertAndSend(docshifterMetricsQueue.getName(), metricsMessage, messagePostProcessor -> {
			log.debug("'jmsTemplate.convertAndSend': metricsMessage.taskId={}",
					metricsMessage::getTaskId);
			return messagePostProcessor;
		});
	}
}
