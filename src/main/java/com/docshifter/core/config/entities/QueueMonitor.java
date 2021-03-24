package com.docshifter.core.config.entities;

import java.nio.file.Path;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class QueueMonitor {

	private static Logger logger = LoggerFactory.getLogger(QueueMonitor.class);

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	private long timeStampStarted;
	private String type;
	private String queue;
	private Long moduleConfigurationId;
	private String taskId;
	private String taskSource;
	private Integer priority;
	private String state;
	private long duration;
	private Boolean success;
	private String errorMessage;
	private String senderHost;
	private String receiverHost;

	public QueueMonitor() {}

	/**
	 * Convenience Constructor for a Queued QueueMonitor record with Task Source as a Path
	 * @param type
	 * @param queue
	 * @param configId
	 * @param taskId
	 * @param taskSource
	 * @param priority
	 * @param senderHost
	 */
	public QueueMonitor(String type, String queue, long configId, String taskId, Path taskSource, int priority, String senderHost) {
		this(type, queue, configId, taskId, taskSource.toString(), priority, senderHost);
	}

	/**
	 * Constructor for a Queued QueueMonitor record.
	 * Be sure to use set...() as the setters have auto-truncation to avoid that we try to store more than 255 characters in our
	 *     varchar(255) fields! Not everything is set here, as this is a Queued record that didin't finish yet... so processing
	 *     time, result (success/fail) etc. are not known yet.
	 * @param type
	 * @param queue
	 * @param configId
	 * @param taskId
	 * @param taskSource
	 * @param priority
	 * @param senderHost
	 */
	public QueueMonitor(String type, String queue, long moduleConfigurationId, String taskId, String taskSource, int priority, String senderHost) {
		this.setTimeStampStarted(Instant.now().getEpochSecond());
		this.setType(type);
		this.setQueue(queue);
		this.setModuleConfigurationId(moduleConfigurationId);
		this.setTaskId(taskId);
		this.setTaskSource(taskSource);
		this.setPriority(priority);
		this.setState(QueueMonitorState.QUEUED.toString());
		this.setSenderHost(senderHost);
	}

	public long getTimeStampStarted() {
		return timeStampStarted;
	}

	public void setTimeStampStarted(long timeStampStarted) {
		this.timeStampStarted = timeStampStarted;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if (type != null && type.length() > 255) {
			logger.warn("Type was truncated to 255 characters. Original: " + type);
			this.type = type.substring(0, 255);
		}
		else {
			this.type = type;
		}
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		if (queue != null && queue.length() > 255) {
			logger.warn("Queue Name was truncated to 255 characters. Original: " + queue);
			this.queue = queue.substring(0, 255);
		}
		else {
			this.queue = queue;
		}
	}

	public Long getModuleConfigurationId() {
		return moduleConfigurationId;
	}

	public void setModuleConfigurationId(Long moduleConfigurationId) {
		this.moduleConfigurationId = moduleConfigurationId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		if (taskId != null && taskId.length() > 255) {
			logger.warn("Task Id was truncated to 255 characters. Original: " + taskId);
			this.taskId = taskId.substring(0, 255);
		}
		else {
			this.taskId = taskId;
		}
	}

	public String getTaskSource() {
		return taskSource;
	}

	public void setTaskSource(String taskSource) {
		if (taskSource != null && taskSource.length() > 255) {
			logger.warn("Task Source was truncated to 255 characters. Original: " + taskSource);
			this.taskSource = taskSource.substring(0, 255);
		}
		else {
			this.taskSource = taskSource;
		}
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getState() {
		return state;
	}

	public void setState(QueueMonitorState state) {
		setState(state.toString());
	}

	public void setState(String state) {
		if (state != null && state.length() > 255) {
			logger.warn("State was truncated to 255 characters. Original: " + state);
			this.state = state.substring(0, 255);
		}
		else {
			this.state = state;
		}
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		if (errorMessage != null && errorMessage.length() > 255) {
			logger.warn("Error Message was truncated to 255 characters. Original: " + errorMessage);
			this.errorMessage = errorMessage.substring(0, 255);
		}
		else {
			this.errorMessage = errorMessage;
		}
	}

	public String getSenderHost() {
		return senderHost;
	}

	public void setSenderHost(String senderHost) {
		this.senderHost = senderHost;
	}

	public String getReceiverHost() {
		return receiverHost;
	}

	public void setReceiverHost(String receiverHost) {
		this.receiverHost = receiverHost;
	}

	@Override
	public String toString() {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("ChainConfiguration = {");
		sBuf.append("Id: ");
		sBuf.append(this.id);
		sBuf.append(", State: ");
		sBuf.append(this.state.toString());
		sBuf.append(", Started: ");
		sBuf.append(this.timeStampStarted);
		sBuf.append(", Sender Host: ");
		sBuf.append(this.senderHost);
		sBuf.append(", Receiver Host: ");
		sBuf.append(this.receiverHost);
		sBuf.append(", Type: ");
		sBuf.append(this.type);
		sBuf.append(", Queue: ");
		sBuf.append(this.queue);
		sBuf.append(", Module Configuration Id: ");
		sBuf.append(this.moduleConfigurationId);
		sBuf.append(", Task Id: ");
		sBuf.append(this.taskId);
		sBuf.append(", Task Source: ");
		sBuf.append(this.taskSource);
		sBuf.append(", Priority: ");
		sBuf.append(this.priority);
		sBuf.append(", Duration: ");
		sBuf.append(this.duration);
		sBuf.append(", Success: ");
		sBuf.append(this.success);
		sBuf.append(", Error Message: ");
		sBuf.append(this.errorMessage);
		sBuf.append("}");
		return sBuf.toString();
	}
}
