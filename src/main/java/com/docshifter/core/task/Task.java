package com.docshifter.core.task;

import com.docshifter.core.work.WorkFolder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Value object which represents a dmi_queue_item object.
 * 
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public class Task implements Serializable {

	private static final long serialVersionUID = 4087826709318179760L;
	protected String id;
	protected WorkFolder workFolder;
	protected Path sourceFilePath;
	protected String name;
	protected String folderStructure;
	protected ArrayList<String> messages = new ArrayList<>();
	protected Map<String, Object> data = new HashMap<>();

	
	public Task() {
	}


	public Task(Path filePath, WorkFolder wf) {
		this.sourceFilePath = filePath;
		this.workFolder = wf;
	}




	public WorkFolder getWorkFolder() {
		return workFolder;
	}

	public void setWorkFolder(WorkFolder workFolder) {
		this.workFolder = workFolder;
	}




	public Path getSourceFilePath() {
		return sourceFilePath;
	}
	public void setSourceFilePath(Path sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	public String getFolderStructure() {
		return folderStructure;
	}
	public void setFolderStructure(String folderStructure) {
		this.folderStructure = folderStructure;
	}

	public ArrayList<String> getMessages() {
		return messages;
	}
	public void addMessage(String message) {
		messages.add(message);
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