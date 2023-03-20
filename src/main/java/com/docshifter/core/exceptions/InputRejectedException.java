package com.docshifter.core.exceptions;

/**
 * More specialized {@link IllegalArgumentException} that can be thrown (mainly from within modules) to signify that
 * an input was rejected.
 */
public class InputRejectedException extends IllegalArgumentException {
	public InputRejectedException() {
		super();
	}

	public InputRejectedException(String message) {
		super(message);
	}

	public InputRejectedException(Throwable cause) {
		super(cause);
	}

	public InputRejectedException(String message, Throwable cause) {
		super(message, cause);
	}
}
