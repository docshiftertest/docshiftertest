package com.docshifter.core.messaging.message;

import com.docshifter.core.task.DctmTask;
import com.docshifter.core.task.Task;

import java.io.Serializable;

/**
 * Created by michiel.vandriessche@docbyte.com on 5/20/16.
 */
public class DocshifterMessage implements Serializable {
	private DocshifterMessageType type;
	private Task task;
	private DctmTask dctmtask;
	private Long configId;

	public DocshifterMessage() {
	}

	public DocshifterMessage(DocshifterMessageType type, DctmTask task, Long configId) {
		this.type = type;
		this.dctmtask = task;
		this.configId = configId;
	}

	public DocshifterMessage(DocshifterMessageType type, Task task, Long configId) {
		this.type = type;
		this.task = task;
		this.configId = configId;
	}

	public Task getTask() {
		return task;
	}

	public DctmTask getDctmTask() {
		return dctmtask;
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
