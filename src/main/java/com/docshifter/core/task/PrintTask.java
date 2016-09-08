package com.docshifter.core.task;

public class PrintTask extends Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5990288169369304994L;


	private String filename;
	
	public PrintTask(){
		super();
	}
	
	public PrintTask(String filePath){
		this.item_id=filePath;
		//this.name="filesystem task";
		//this.item_id=filePath;
		//this.name=filePath;
	}

	public String getFilename() {
		return filename;
	}
	public String getFilePath(){
		return this.item_id;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setFilePath(String filename) {
		this.item_id = filename;
	}



}
