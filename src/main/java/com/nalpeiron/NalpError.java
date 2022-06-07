//
// NalpError.java
//
// Created Jan 18 2013
// R. D. Ramey
//

package com.nalpeiron;

/**
 * Error handling routines for Nalpeiron V10 Java classes
 */
public class NalpError extends Error {
	static final long serialVersionUID = 999L;

	//	This will store the error code returned by the NALP function
	private final int NalpErrorCode;
	private String NalpErrorMsg;

	/**
	 * Default constructor
	 */
	public NalpError() {
		super("Undefined Error");
		NalpErrorCode = 0;
	}

	/**
	 * Regular constructor
	 *
	 * @param i   The error code that was returned by the Nalp function (\ref V10ERROR)
	 * @param msg A descriptive error message
	 */
	public NalpError(int i, String msg) {
		super("License error code " + i + ": " + msg);
		NalpErrorCode = i;
		NalpErrorMsg = msg;
	}


	public String getErrorMessage() {
		return NalpErrorMsg;
	}

	/**
	 * Returns the error code of the Nalp function
	 *
	 * @return The error code of the Nalp function.
	 * Should always be negative (\ref V10ERROR)
	 */
	public int getErrorCode() {
		return NalpErrorCode;
	}
}

//end of NalpError 

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
