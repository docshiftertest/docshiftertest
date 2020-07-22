package com.docshifter.core.task;

import com.docshifter.core.work.WorkFolder;

import java.nio.file.Path;

public class SyncTask extends Task {
	
	private static final long serialVersionUID = -419818551015529367L;
	protected Path outputFilePath;
	protected String fileName;
	protected boolean discardReturnFile;

	public SyncTask() { }

	public SyncTask(String filePath, WorkFolder wf, boolean discardReturnFile) {
		super(filePath, wf);
		this.discardReturnFile = discardReturnFile;
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

	public boolean isDiscardReturnFile() {
		return discardReturnFile;
	}
}
