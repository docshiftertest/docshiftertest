package com.docshifter.core.asposehelper.adapters;

import java.nio.file.Path;

class WordDocumentAdapterTest extends DocumentAdapterTest<WordDocumentAdapter> {
	@Override
	WordDocumentAdapter createSut(Path path) throws Exception {
		return new WordDocumentAdapter(path);
	}

	@Override
	String getExtension() {
		return "docx";
	}
}
