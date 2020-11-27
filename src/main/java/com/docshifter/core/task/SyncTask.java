package com.docshifter.core.task;

import com.docshifter.core.work.WorkFolder;

import java.nio.file.Path;

public class SyncTask extends Task {
	
	private static final long serialVersionUID = -419818551015529367L;
	protected Path outputFilePath;
	protected String fileName;
	protected boolean discardReturnFile;
	protected boolean keepReleasePathFiles;

	public SyncTask() { }

	public SyncTask(Path filePath, WorkFolder wf, boolean discardReturnFile, boolean keepReleasePathFiles) {
		super(filePath, wf);
		this.discardReturnFile = discardReturnFile;
		this.keepReleasePathFiles = keepReleasePathFiles;
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

	public boolean isKeepReleasePathFiles() {
		return keepReleasePathFiles;
	}
}
