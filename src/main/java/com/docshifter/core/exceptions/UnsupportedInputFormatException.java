package com.docshifter.core.exceptions;

/**
 * More specialized {@link UnsupportedOperationException} that can be thrown (mainly from within modules) to signify that the
 * provided input is in a format that is not supported by the module.
 */
public class UnsupportedInputFormatException extends UnsupportedOperationException {
	public UnsupportedInputFormatException() {
		super();
	}

	public UnsupportedInputFormatException(String message) {
		super(message);
	}

	public UnsupportedInputFormatException(Throwable cause) {
		super(cause);
	}

	public UnsupportedInputFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
