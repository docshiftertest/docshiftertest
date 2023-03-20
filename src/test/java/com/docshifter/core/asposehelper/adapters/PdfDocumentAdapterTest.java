package com.docshifter.core.asposehelper.adapters;

import java.nio.file.Path;

class PdfDocumentAdapterTest extends DocumentAdapterTest<PdfDocumentAdapter> {
	@Override
	PdfDocumentAdapter createSut(Path path) {
		return new PdfDocumentAdapter(path, 60, 60);
	}

	@Override
	String getExtension() {
		return "pdf";
	}
}
