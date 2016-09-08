package com.docshifter.core.task;

import com.docshifter.core.work.WorkFolder;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class Task implements Serializable {

	private static final long serialVersionUID = 4087826709318179760L;
	protected String item_id;    //unique file identifier: default from dctm= objectid
	protected WorkFolder workFolder;
	protected Path sourceFilePath;
	protected Path folderStructure;
	protected ArrayList<String> messages;


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


	public Task(String filePath, WorkFolder wf) {
		this.item_id = filePath;
		this.workFolder = wf;
	}


	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
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

	public Path getFolderStructure() {
		return folderStructure;
	}
	public void setFolderStructure(Path folderStructure) {
		this.folderStructure = folderStructure;
	}

	public ArrayList<String> getMessages() {
		return messages;
	}
	public void addMessage(String message) {
		messages.add(message);
	}

}