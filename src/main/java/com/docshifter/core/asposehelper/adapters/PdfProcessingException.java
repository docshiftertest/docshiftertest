package com.docshifter.core.asposehelper.adapters;

public class PdfProcessingException extends RuntimeException {
	public PdfProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public PdfProcessingException(Throwable cause) {
		super(cause);
	}
}
