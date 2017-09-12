package com.docshifter.core.task;

import com.docshifter.core.work.WorkFolder;

import java.nio.file.Path;

public class SyncTask extends Task {
	
	
	protected Path outputFilePath;
	protected String fileName;

	public SyncTask() {	}

	public SyncTask(Path filePath, WorkFolder wf) {
		super(filePath, wf);
	}
	
	public Path getOutputFilePath() {
		return outputFilePath;
	}
	
	public void setOutputFilePath(Path outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
