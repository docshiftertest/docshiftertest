package com.docshifter.core.exceptions;

public class MoveAlong extends Exception {
    public MoveAlong() {
        super();
    }

    public MoveAlong(Throwable cause) {
        super(cause);
    }

    public MoveAlong(String message) {
        super(message);
    }

    public MoveAlong(String message, Throwable cause) {
        super(message, cause);
    }

    public MoveAlong(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
