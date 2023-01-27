package com.docshifter.core.exceptions;

/**
 * {@link RuntimeException} that can be thrown (mainly from within modules) to signify that a configuration value is badly
 * formatted or conflicting with other configuration values.
 */
public class BadConfigException extends RuntimeException {

    public BadConfigException() {
        super();
    }

    public BadConfigException(String message) {
        super(message);
    }

    public BadConfigException(Throwable cause) {
        super(cause);
    }

    public BadConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
