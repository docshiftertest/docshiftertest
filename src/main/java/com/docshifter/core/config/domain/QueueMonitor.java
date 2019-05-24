package com.docshifter.core.config.domain;

import java.nio.file.Path;
import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class QueueMonitor {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	private long timeStampStarted;
	private String type;
	private String queue;
	private Long configId;
	private String taskId;
	private String taskSource;
	private Integer priority;
	private String state;
	private long duration;
	private Boolean success;
	private String errorMessage;

	public QueueMonitor() {}

	public QueueMonitor(String type, String queue, long configId, String taskId, Path taskSource, int priority) {
		this(type, queue, configId, taskId, taskSource.toString(), priority);
	}

	public QueueMonitor(String type, String queue, long configId, String taskId, String taskSource, int priority) {
		this.timeStampStarted = Instant.now().getEpochSecond();
		this.type = type;
		this.queue = queue;
		this.configId = configId;
		this.taskId = taskId;
		this.taskSource = taskSource;
		this.priority = priority;
		this.state = QueueMonitorState.QUEUED.toString();
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
		this.type = type;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public Long getConfigId() {
		return configId;
	}

	public void setConfigId(Long configId) {
		this.configId = configId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskSource() {
		return taskSource;
	}

	public void setTaskSource(String taskSource) {
		this.taskSource = taskSource;
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
		this.state = state;
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
		this.errorMessage = errorMessage;
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
		sBuf.append(", Type: ");
		sBuf.append(this.type);
		sBuf.append(", Queue: ");
		sBuf.append(this.queue);
		sBuf.append(", Config Id: ");
		sBuf.append(this.configId);
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
