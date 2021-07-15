package com.docshifter.core.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.docshifter.core.work.WorkFolder;

public class Task implements Serializable {

	private static final long serialVersionUID = 4087826709318179760L;
	protected String id;
	protected WorkFolder workFolder;
	protected String sourceFilePath;
	protected String name;
	protected String folderStructure;
	protected final List<String> messages = new ArrayList<>();
	protected final Map<String, Object> data = new HashMap<>();
	protected final List<String> extraFilesList = new ArrayList<>();

	public List<String> getExtraFilesList() {
		return extraFilesList;
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
		return messages;
	}
	public String addMessage(String message) {
		messages.add(message);
		return message;
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
	
	@Override
	public String toString() {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("Task{id='");
		sBuf.append(id);
		sBuf.append("', workFolder='");
		sBuf.append(workFolder);
		sBuf.append("', sourceFilePath=");
		sBuf.append(sourceFilePath);
		sBuf.append("', name='");
		sBuf.append(name);
		sBuf.append("', folderStructure=");
		sBuf.append(folderStructure);
		sBuf.append("', messages=[");
		for (String message : messages) {
			sBuf.append("'");
			sBuf.append(message);
			sBuf.append("', ");
		}
		if (messages.size() > 0) {
			sBuf.setLength(sBuf.length() - 3);
		}
		sBuf.append("], data=");
		sBuf.append(data);
		sBuf.append("'}");
		return sBuf.toString();
	}
}
