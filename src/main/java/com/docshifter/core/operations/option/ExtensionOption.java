package com.docshifter.core.operations.option;

import java.nio.file.Path;

public class ExtensionOption extends AbstractOption<String> {

	protected String getResult() {
		Path inFilePath = operationParams.getSourcePath();
		String filename = inFilePath.getFileName().toString().toUpperCase();
		return (filename.substring(filename.lastIndexOf(".")+1));
	}
}
