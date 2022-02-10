package com.docshifter.core.logging.appenders;

import com.docshifter.core.task.Task;
import com.docshifter.core.task.TaskMessageSeverity;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Log4j2 Appender plugin that automatically adds messages to a task based on log statements.
 */
@Plugin(name = "TaskMessage",
		category = Core.CATEGORY_NAME,
		elementType = Appender.ELEMENT_TYPE)
public class TaskMessageAppender extends AbstractAppender {

	private static final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();
	private static final Map<Level, TaskMessageSeverity> logLevelMappings = new HashMap<>();
	static {
		logLevelMappings.put(Level.INFO, TaskMessageSeverity.INFORMATION);
		logLevelMappings.put(Level.WARN, TaskMessageSeverity.WARNING);
		logLevelMappings.put(Level.ERROR, TaskMessageSeverity.ERROR);
		logLevelMappings.put(Level.FATAL, TaskMessageSeverity.ERROR);
	}

	protected TaskMessageAppender(String name, Filter filter, Layout<? extends Serializable> layout,
								  boolean ignoreExceptions, Property[] properties) {
		super(name, filter, layout, ignoreExceptions, properties);
	}

	@PluginFactory
	public static TaskMessageAppender createAppender(@PluginAttribute("name") String name,
													 @PluginElement("Filters") Filter filter,
													 @PluginElement("Layout") Layout<? extends Serializable> layout,
													 @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
													 @PluginElement("Properties") Property[] properties
	) {
		return new TaskMessageAppender(name, filter, layout, ignoreExceptions, properties);
	}

	/**
	 * Adds a task to be tracked and associates the current/calling thread with it. Should preferably be called once
	 * whenever a task is initially created.
	 * @param task The task to start tracking.
	 */
	public static void trackTask(Task task) {
		registerCurrentThread(task);
		tasks.put(task.getId(), task);
	}

	/**
	 * Associates the current/calling thread with a particular task. Should be called inside a new thread whenever one
	 * is spawned in a module or wherever there is a task context.
	 * @param task The task to create an association with.
	 */
	public static void registerCurrentThread(Task task) {
		ThreadContext.put("taskId", task.getId());
	}

	/**
	 * Removes a task from the tracking map based on whatever task is associated with the current thread, and also
	 * gets rid of this association. Should be called once whenever a task is about to become irrelevant (whenever it
	 * goes out of scope for garbage collection).
	 */
	public static void untrackTask() {
		String taskId = ThreadContext.get("taskId");
		if (taskId != null) {
			ThreadContext.remove("taskId");
			tasks.remove(taskId);
		}
	}

	/**
	 * Gets called whenever a log event happens, in which case we should add a message to the task if appropriate.
	 * @param logEvent The supplied log event by Log4j2.
	 */
	@Override
	public void append(LogEvent logEvent) {
		TaskMessageSeverity severity = logLevelMappings.get(logEvent.getLevel());
		if (severity == null) {
			return;
		}
		String taskId = logEvent.getContextData().getValue("taskId");
		if (taskId == null) {
			error("Task ID was null on the logging context data map, so could not add a task message! Please contact " +
					"DocShifter for support.");
			return;
		}
		Task task = tasks.get(taskId);
		if (task == null) {
			error("Task was not found, so could not add a task message! Please contact DocShifter for support.");
			return;
		}
		task.addMessage(severity, logEvent.getMessage().getFormattedMessage());
	}
}
