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
	protected List<String> messages = new ArrayList<>();
	protected Map<String, Object> data = new HashMap<>();

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

	public void setData(Map<String, Object> data) {
		this.data = data;
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
		return "Task{" +
				"id='" + id + '\'' +
				", workFolder=" + workFolder +
				", sourceFilePath=" + sourceFilePath +
				", name='" + name + '\'' +
				", folderStructure='" + folderStructure + '\'' +
				", messages=" + messages +
				", data=" + data +
				'}';
	}
}