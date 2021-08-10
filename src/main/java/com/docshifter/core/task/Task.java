package com.docshifter.core.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.docshifter.core.work.WorkFolder;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@ToString
@Log4j2
public class Task implements Serializable {

	private static final long serialVersionUID = 4087826709318179760L;
	protected String id;
	protected WorkFolder workFolder;
	protected String sourceFilePath;
	protected String name;
	protected String folderStructure;
	protected final Map<TaskMessageSeverity, List<String>> messages = new HashMap<>();
	protected final Map<String, Object> data = new HashMap<>();
	protected final List<String> extraFilesList = new ArrayList<>();
	protected final List<String> configFilesList = new ArrayList<>();

	public List<String> getExtraFilesList() {
		return extraFilesList;
	}

	public List<String> getConfigFilesList() {
		return configFilesList;
	}

	public Task() {
	}

	public Task(String filePath, WorkFolder wf) {
		this.sourceFilePath = filePath;
		this.workFolder = wf;
	}

	public WorkFolder getWorkFolder() {
		return workFolder;
	}

	public void setWorkFolder(WorkFolder workFolder) {
		this.workFolder = workFolder;
	}

	public String getSourceFilePath() {
		return sourceFilePath;
	}
	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	public String getFolderStructure() {
		return folderStructure;
	}
	public void setFolderStructure(String folderStructure) {
		this.folderStructure = folderStructure;
	}

	public List<String> getMessages() {
		List<String> results = new ArrayList<>();
		for (TaskMessageSeverity severity : messages.keySet()) {
			results.addAll(getMessages(severity));
		}
		return results;
	}

	public List<String> getMessages(TaskMessageSeverity severity) {
		List<String> results = new ArrayList<>();
		if (messages.containsKey(severity)) {
			for (String message : messages.get(severity)) {
				results.add(severity.name() + ": " + message);
			}
		}
		return results;
	}

	public String addMessage(TaskMessageSeverity severity, String message) {
		List<String> messagesForSeverity = messages.get(severity);
		if (messagesForSeverity == null) {
			messagesForSeverity = new ArrayList<>();
		}
		messagesForSeverity.add(message);
		messages.put(severity, messagesForSeverity);
		return message;
	}

	public String addMessage(String message) {
		return addMessage(TaskMessageSeverity.ERROR, message);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void addData(String identifier, Object dataObject){
		data.put(identifier, dataObject);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
