//
// nalpError.java
//
// Created Jan 18 2013
// R. D. Ramey
//

package com.nalpeiron.nalplibrary;

public class
nalpError extends Error
{
	static final long serialVersionUID = 999L;

	//	This will store the error code returned by the NALP function
	private	int			nalpErrorCode;
	private String		nalpErrorMsg;

/**
 * Default constructor
 */
	public
	nalpError(
	)
	{
		super("Undefined Error");
		nalpErrorCode = 0;
	}

/**
 * Regular constructor
 * @param i:	The error code that was returned by the Nalp function
 */
	public
	nalpError(
	int i,
	String msg
	)
	{
		nalpErrorCode = i;
		nalpErrorMsg = msg;

		System.out.println("Error " + i + " " + msg);
	}


	public String
	getErrorMessage(
	)
	{
		return nalpErrorMsg;
	}

/**
 * Returns the error code of the Nalp function
 * @return	The error code of the Nalp function.  Should always be negative
 */
	public int
	getErrorCode(
	)
	{
		return nalpErrorCode;
	}
}


/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
