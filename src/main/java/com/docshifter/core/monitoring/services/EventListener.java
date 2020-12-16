package com.docshifter.core.monitoring.services;

import com.docshifter.core.events.NotificationEvent;
import com.docshifter.core.task.Task;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventListener implements ApplicationListener<NotificationEvent> {

	private Map<String,Task> mapOfTasks = new HashMap<>();
 
	@Override
	public void onApplicationEvent(NotificationEvent event) {
		mapOfTasks.put(event.getTask().getId(), event.getTask());
	}

	public Map<String,Task>  getMapOfTasks() {
		return mapOfTasks;
	}
}
