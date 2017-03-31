package com.docshifter.core.messaging.message;

import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.Task;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Created by michiel.vandriessche@docbyte.com on 5/20/16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public class DocshifterMessage implements Serializable {
	private DocshifterMessageType type;
	private Task task;
	private Long configId;

	public DocshifterMessage() {
	}


	public DocshifterMessage(DocshifterMessageType type, Task task, Long configId) {
		this.type = type;
		this.task = task;
		this.configId = configId;
	}

	public Task getTask() {
		return task;
	}


	public DocshifterMessageType getType() {
		return type;
	}

	public Long getConfigId() {
		return configId;
	}

	@Override
	public String toString() {
		return "DocshifterMessage{" +
				"configId=" + configId +
				", type=" + type +
				", task=" + task +
				'}';
	}
}
