package com.docshifter.core.exceptions;

/**
 * {@link Exception} that can be thrown (mainly from within modules) to signify that a configuration value is badly
 * formatted or conflicting with other configuration values.
 */
public class InvalidConfigException extends Exception {
	public InvalidConfigException() {
		super();
	}

	public InvalidConfigException(String message) {
		super(message);
	}

	public InvalidConfigException(Throwable cause) {
		super(cause);
	}

	public InvalidConfigException(String message, Throwable cause) {
		super(message, cause);
	}
}
