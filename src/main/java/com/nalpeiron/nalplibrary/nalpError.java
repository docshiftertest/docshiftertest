//
// nalpError.java
//
// Created Jan 18 2013
// R. D. Ramey
//
// based on NLS version by Jeremy Porath
// Created April 26, 2010
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
 * @param i:	The error code that was returned by the Nalpeiron function
 */
	public 
	nalpError(
	int i, 
	String msg
	)
	{
		nalpErrorCode = i;
		nalpErrorMsg = msg;
	}

/**
 * Constructs an error message based on the error code passed in
 * @param i:	The error code that was returned by the Nalpeiron function
 * @return:		A string that can be used as a possible error message to show the user
 */
	public String 
	getErrorMessage(
	)
	{
		return nalpErrorMsg;
	}

/**
 * Returns the error code of the Nalpeiron function
 * @return	The error code of the Nalpeiron function.  Should always be negative
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
