package com.docbyte.docshifter.utils.exceptions;

/**
 * Created by michiel.vandriessche@docbyte.com on 4/13/15.
 */
public class UnableToProcesException extends Exception {


	public UnableToProcesException() {
		super();
	}

	public UnableToProcesException(Throwable cause) {
		super(cause);
	}

	public UnableToProcesException(String message) {
		super(message);
	}

	public UnableToProcesException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnableToProcesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
