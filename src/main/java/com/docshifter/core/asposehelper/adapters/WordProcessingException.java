package com.docshifter.core.asposehelper.adapters;

public class WordProcessingException extends RuntimeException {
	public WordProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public WordProcessingException(Throwable cause) {
		super(cause);
	}
}
