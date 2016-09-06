/**
 * 
 */
package com.docshifter.core.exceptions;

/**
 * @author dieter.verlinde
 *
 */
public class EmptyOperationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1332341808527585262L;

	/**
	 * 
	 */
	public EmptyOperationException() {
	}

	/**
	 * @param arg0
	 */
	public EmptyOperationException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public EmptyOperationException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public EmptyOperationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
