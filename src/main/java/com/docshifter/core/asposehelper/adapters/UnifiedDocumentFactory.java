package com.docshifter.core.asposehelper.adapters;

import com.docshifter.core.utils.FileUtils;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor
public class UnifiedDocumentFactory {
	private final double headerMargin;
	private final double footerMargin;

	public UnifiedDocument getDocument(Path path) {
		String extension = FileUtils.getExtension(path);
		return switch (extension.toLowerCase()) {
			case "doc", "docm", "docx", "dot", "dotm", "dotx", "odt" -> new WordDocumentAdapter(path);
			case "pdf" -> new PdfDocumentAdapter(path, headerMargin, footerMargin);
			default -> throw new UnsupportedOperationException("Unsupported document format: " + extension);
		};
	}
}
