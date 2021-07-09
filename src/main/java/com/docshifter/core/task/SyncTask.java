package com.docshifter.core.task;

import com.docshifter.core.work.WorkFolder;

public class SyncTask extends Task {
	
	private static final long serialVersionUID = -419818551015529367L;
	protected String outputFilePath;
	protected String fileName;
	protected boolean discardReturnFile;
	protected boolean keepReleasePathFiles;

	public SyncTask() { }

	public SyncTask(String filePath, WorkFolder wf, boolean discardReturnFile, boolean keepReleasePathFiles) {
		super(filePath, wf);
		this.discardReturnFile = discardReturnFile;
		this.keepReleasePathFiles = keepReleasePathFiles;
	}
	
	public String getOutputFilePath() {
		return outputFilePath;
	}
	
	public void setOutputFilePath(String outputFilePath) {
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

	public boolean isKeepReleasePathFiles() {
		return keepReleasePathFiles;
	}
}
