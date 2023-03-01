package com.docshifter.core.asposehelper.adapters;

import com.docshifter.core.exceptions.InputCorruptException;
import com.docshifter.core.exceptions.UnsupportedInputFormatException;
import com.docshifter.core.utils.FileUtils;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

@AllArgsConstructor
public class UnifiedDocumentFactory {
	private final double headerMargin;
	private final double footerMargin;

	public UnifiedDocument getDocument(Path path) {
		String extension = FileUtils.getExtension(path);
		try {
			return switch (extension.toLowerCase()) {
				case "doc", "docm", "docx", "dot", "dotm", "dotx", "odt" -> new WordDocumentAdapter(path);
				case "pdf" -> new PdfDocumentAdapter(path, headerMargin, footerMargin);
				default -> throw new UnsupportedInputFormatException("Unsupported document format: " + extension);
			};
		} catch (UnsupportedInputFormatException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InputCorruptException("Could not load document at path: " + path, ex);
		}
	}
}
