package com.docshifter.core.task;

import java.nio.file.Path;

public class PrintTask extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5990288169369304994L;


	private String filename;
	private String sentBy;
	
	public PrintTask(){
		super();
	}
	
	public PrintTask(String filePath){
		this.sourceFilePath=filePath;
		//this.name="filesystem task";
		//this.item_id=filePath;
		//this.name=filePath;
	}

	public String getFilename() {
		return filename;
	}
	public String getFilePath(){
		return this.sourceFilePath;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setFilePath(String filename) {
		this.sourceFilePath = filename;
	}
	public String getSentBy(){
		return this.sentBy;
	}
	public void setSentBy(String sentBy) {
		this.sentBy = sentBy;
	}
}
