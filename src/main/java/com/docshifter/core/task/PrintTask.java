package com.docshifter.core.task;

import java.nio.file.Path;

public class PrintTask extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5990288169369304994L;


	private String filename;
	
	public PrintTask(){
		super();
	}
	
	public PrintTask(Path filePath){
		this.sourceFilePath=filePath;
		//this.name="filesystem task";
		//this.item_id=filePath;
		//this.name=filePath;
	}

	public String getFilename() {
		return filename;
	}
	public Path getFilePath(){
		return this.sourceFilePath;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setFilePath(Path filename) {
		this.sourceFilePath = filename;
	}



}
