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


	//For the Russia Time Stamping
/*	public Task() {

		//08 June 2016
		final long end = 1465992000;
		final long now = System.currentTimeMillis()/1000L;

		//01 March 2016
		final long start = 1456833600;

		Logger.info("THIS TRIALVERSION WILL END ON 01/06/2016", null);
		if (now-start < 0) {
			Logger.info("TRIAL EXPIRED", null);
			System.exit(9);
		}

		if (now > end) {
			Logger.info("TRIAL EXPIRED", null);
			System.exit(9);
		}
	}
*/
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


	//temp to show sending a task through jms works (ObjectMessage)
	public String toString() {

		return sourceFilePath.toAbsolutePath().toString();
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
}