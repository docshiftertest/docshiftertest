//
//nalp.java
//
// Created Jan 19 2012
// R. D. Ramey


/**
 *  @defgroup PASSIVEJAVA Nalpeiron Passive Java classes 
 *  @defgroup SIMONJAVA Nalpeiron Passive library initialization and generic functions
 *  @ingroup V10
 *  @ingroup PASSIVEJAVA
 *  @{
 */

/**
 *  @file   passlibrary/NALP.java
 *  @brief  Generic and initialization functions for Nalpeiron Passive library
 */

package com.nalpeiron.passlibrary;

import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import com.nalpeiron.NalpError;
import lombok.extern.log4j.Log4j2;

import java.io.UnsupportedEncodingException;

@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
public class NALP
{
	//Library open function
	private native int PSLLibOpen(byte[] xmlParams);

	//Library close function
	private native int PSLLibClose();

	//Turn NSA/NSL error returns into nalpative strings
	private native String PSLGetErrorMsg(int nalpErrNo);

	// Open the JNI wrapper library. Use static initialization block
	// so that we only do this once no matter how many NALPs are created
	static {
		try {
			System.loadLibrary("PassiveFilechck");
		} catch (Exception ex) {
			log.warn("Tried to load PassiveFilechck native lib but it failed. Subsequent JNI calls will fail. Is it " +
					"inaccessible?", ex);
		}
	}

/**
 * @brief Initializes the Nalpeiron C library for use.  See NalpLibOpen().
 *
 * @param LogLevel is an integer 0 to 6. If loglevel is not specified it will
 * default to level 0 (off). You should not use log level higher than 4.
 * The higher levels do not provide any useful end user debugging.
 *
 * @param LicenseCode the license code for this product
 *
 * @param WorkDir is the location where the library will store all its working files
 * (ie log file, cache file, license files, etc).  If this value is not
 * specified the files will be stored in the default Nalpeiron directory
 * (cwd, or current working directory) on Linux and Mac, in a publically
 * accessible directory selected by the OS on Windows).
 *
 * @param LogQLen the maximum length of the logging queue.
 *
 * @param security is used along with the authentication values stamped
 * into the library to create a unique offset that is added to all returns.
 * The default is 0 (ie returns from the library will NOT be modified).
 *
 * @throws NalpError :   If there was a problem initializing the library
 *                      this error will be thrown
 * @return = 0 If the call succeeded
 * @return < 0 A negative error value is returned (\ref V10ERROR)
 */
	public int
	callPSLLibOpen(int LogLevel, String LicenseCode,
			String WorkDir, int LogQLen, int security)
	throws NalpError
	{
		int			i;
		long		lhandle_t[];
		String		xmlParams;


		lhandle_t = new	long[1];

		//Construct PSLLibOpen's xml parameter
		xmlParams = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlParams = xmlParams + "<SHAFERXMLParams>";

		xmlParams = xmlParams +
			"<SecurityValue>" + security + "</SecurityValue>";

		//A license Code MUST be specified.  however PSLLibOpen will pick it up
		// and error out if missing.
		if (!LicenseCode.equals(""))
		{
			xmlParams = xmlParams + "<LicenseCode>" + LicenseCode+ "</LicenseCode>";
		}

		if (!WorkDir.equals(""))
		{
			xmlParams = xmlParams + "<WorkDir>" + WorkDir + "</WorkDir>";
		}

		if ((LogLevel < 0) || (LogLevel > 5))
		{
			LogLevel = 0;
		}

		xmlParams = xmlParams + "<LogLevel>" + LogLevel + "</LogLevel>";

		if (LogQLen <= 0)
		{
			LogQLen = 300;
		}

		xmlParams = xmlParams + "<LogQLen>" + LogQLen + "</LogQLen>";

		xmlParams = xmlParams + "</SHAFERXMLParams>";

		try
		{
			i = PSLLibOpen((xmlParams + '\000').getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding", e);
		}

		if (i < 0)
		{
			//throw new NalpError(i, PSLGetErrorMsg(i));
			log.error("Error " + i + ": " + callPSLGetErrorMsg(i));
		}

		return i;
	}


/**
 * @brief Shuts down the library. THIS FUNCTION MUST BE CALLED IMMEDIATELY
 * BEFORE CLOSING THE LIBRARY (ie with dlclose). If this function is not
 * called before the library is closed, information may be lost and memory
 * corruption could occur.
 *
 * @throws NalpError:   If there was a problem calling the NSA function,
 *  this error will be thrown
 *
 * @return = 0 If the call succeeded
 *
 * @throws NalpError:   If there was a problem calling the NSL function,
 *  this error will be thrown. (\ref V10ERROR)
 */
	public int
	callPSLLibClose()
	throws NalpError
	{
		int	i;

		i = PSLLibClose();

		if (i < 0)
		{
			throw new NalpError(i, PSLGetErrorMsg(i));
		}

		return i;
	}

/**
 * @brief Get a descriptive string for nalpeiron error codes.
 *
 * @param nalpErrorNo is a negative return value from one of the Nalpeiron
 * functions
 *
 * @return a descriptive string explaining that error.
 */
	public String
	callPSLGetErrorMsg(int nalpErrorNo)
	throws NalpError
	{
		String 	nalpErrorMsg;


		nalpErrorMsg = PSLGetErrorMsg(nalpErrorNo);

		return new String(nalpErrorMsg);
	}
}


/** @} */ //end of NALP

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
