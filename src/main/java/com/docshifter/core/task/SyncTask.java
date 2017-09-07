package com.docshifter.core.task;

import com.docshifter.core.work.WorkFolder;

import java.nio.file.Path;

public class SyncTask extends Task {
	
	
	protected Path outputFilePath;
	
	public SyncTask(Path filePath, WorkFolder wf) {
		super(filePath, wf);
	}
	
	public Path getOutputFilePath() {
		return outputFilePath;
	}
	
	public void setOutputFilePath(Path outputFilePath) {
		this.outputFilePath = outputFilePath;
	}
}
