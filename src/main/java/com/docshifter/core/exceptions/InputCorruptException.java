package com.docshifter.core.exceptions;

public class InputCorruptException extends Exception {
    public InputCorruptException() {
        super();
    }

    public InputCorruptException(String message) {
        super(message);
    }

    public InputCorruptException(Throwable cause) {
        super(cause);
    }

    public InputCorruptException(String message, Throwable cause) {
        super(message, cause);
    }
}
