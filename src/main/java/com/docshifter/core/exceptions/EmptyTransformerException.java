/**
 * 
 */
package com.docbyte.docshifter.receiver.utils;

/**
 * @author dieter.verlinde
 *
 */
public class EmptyTransformerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2917774643650672602L;

	/**
	 * 
	 */
	public EmptyTransformerException() {
	}

	/**
	 * @param arg0
	 */
	public EmptyTransformerException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public EmptyTransformerException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public EmptyTransformerException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
