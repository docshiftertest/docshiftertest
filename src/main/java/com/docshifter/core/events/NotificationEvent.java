package com.docshifter.core.events;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.task.Task;

@Component
public class NotificationEvent extends ApplicationEvent {

	private Task task;

	public NotificationEvent(DocshifterMessage source) {
		super(source);
		this.task = source.getTask();
	}

	public Task getTask() {
		return task;
	}

}