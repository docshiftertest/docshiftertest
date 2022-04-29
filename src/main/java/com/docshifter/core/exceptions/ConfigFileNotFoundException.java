package com.docshifter.core.exceptions;

import java.io.FileNotFoundException;

/**
 * More specialized {@link FileNotFoundException} that can be thrown (mainly from within modules) to signify that a
 * referred configuration file was not found.
 */
public class ConfigFileNotFoundException extends FileNotFoundException {
	public ConfigFileNotFoundException() {
		super();
	}

	public ConfigFileNotFoundException(String message) {
		super(message);
	}

	public ConfigFileNotFoundException(Throwable cause) {
		super();
		super.initCause(cause);
	}

	public ConfigFileNotFoundException(String message, Throwable cause) {
		super(message);
		super.initCause(cause);
	}
}
