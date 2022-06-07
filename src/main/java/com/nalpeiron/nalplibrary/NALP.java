//
//nalp.java
//
// Created Jan 19 2012
// R. D. Ramey

/**
 *  @defgroup V10Java Nalpeiron V10 Java classes 
 *  @defgroup NALPJAVA Nalpeiron V10 library initialization and generic functions
 *  @ingroup V10Java 
 *  @ingroup V10
 *  @{
 */

/**
 *  @file   nalplibrary/NALP.java
 *  @brief  Generic and initialization functions for Nalpeiron V10 library
 */

package com.nalpeiron.nalplibrary;

import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import com.nalpeiron.NalpError;
import lombok.extern.log4j.Log4j2;

import java.io.UnsupportedEncodingException;

@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
public class NALP
{
	//Library open function
	private native int NalpLibOpen(byte[] xmlParams);

	//Library close function
	private native int NalpLibClose();

	//Turn NSA/NSL error returns into nalpative strings
	private native String NalpGetErrorMsg(int nalpErrNo);

	// Open the JNI wrapper library. Use static initialization block
	// so that we only do this once no matter how many NALPs are created
	static {
		try {
			System.loadLibrary("ShaferFilechck");
		} catch (Exception ex) {
			log.warn("Tried to load ShaferFilechck native lib but it failed. Subsequent JNI calls will fail. Is it " +
					"inaccessible?", ex);
		}
	}

/**
 * @brief Initializes the Nalpeiron C library for use.  See NalpLibOpen().
 *
 * @param NSAEnable If TRUE NSA (analytics) library is enabled for use.
 *
 * @param NSLEnable If TRUE NSL (licensing) library is enabled for use.
 *
 * @param LogLevel is an integer 0 to 6. If loglevel is not specified it will
 * default to level 0 (off). You should not use log level higher than 4.
 * The higher levels do not provide any useful end user debugging.
 *
 * @param WorkDir is the location where the library will store all its working files
 * (ie log file, cache file, license files, etc).  If this value is not
 * specified the files will be stored in the default Nalpeiron directory
 * (cwd, or current working directory) on Linux and Mac, in a publically
 * accessible directory selected by the OS on Windows).
 *
 * @param clientName an optional string that can be used to identify the 
 * client system.  This string will appear in DataStream data.
 *
 * @param LogQLen the maximum length of the logging queue.
 *
 * @param CacheQLen is the maximum length of the cache queue. Used only by the
 * NSA section of the library.
 *
 * @param NetThMin The minimum number of threads in the networking thread pool.
 *
 * @param NetThMax The maximum number of threads in the networking thread pool.
 *
 * @param OfflineMode Used only by the NSA library.  See NSA.USAGE for details.
 *
 * @param ProxyIP The IP address of any intervening proxy that is to be
 * used by the library.
 *
 * @param ProxyPort The port number of any intervening proxy that is to be
 * used by the library.
 *
 * @param ProxyUsername The username for any intervening proxy that is to
 * be used by the library.
 *
 * @param ProxyPass The password for any intervening proxy that is to
 * be used by the library.
 *
 * @param DaemonIP The IP address of the daemon.
 *
 * @param DaemonPort The Port number of the daemon.
 *
 * @param DaemonUser Daemon username
 *
 * @param DaemonPass Daemon password
 *
 * @param security is used along with the authentication values stamped
 * into the library to create a unique offset that is added to all returns.
 * The default is 0 (ie returns from the library will NOT be modified).
 *
 * @throws NalpError:	If there was a problem initializing the library
 * 						this error will be thrown
 * @return = 0 If the call succeeded
 * @return < 0 A negative error value is returned (\ref V10ERROR)
 */
	public int
	callNalpLibOpen(boolean NSAEnable,
		boolean NSLEnable, int LogLevel, String WorkDir, String clientName,
		int LogQLen, int CacheQLen, int NetThMin, int NetThMax,
		int OfflineMode, String ProxyIP, String ProxyPort,
		String ProxyUsername, String ProxyPass, String DaemonIP,
		String DaemonPort, String DaemonUser, String DaemonPass, int security)
	throws NalpError
	{
		int			i;
		long		lhandle_t[];
		String		xmlParams;


		lhandle_t = new	long[1];

		//Construct NalpLibOpen's xml parameter
		xmlParams = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlParams = xmlParams + "<SHAFERXMLParams>";

		//Default for each library (NSA and NSL) is disabled.  You
		// can enable one or the other or both.
		//
		// Enable NSA
		if (NSAEnable == true)
		{
			xmlParams = xmlParams + "<NSAEnabled>1</NSAEnabled>";
		}

		//Enable NSL
		if (NSLEnable == true)
		{
			xmlParams = xmlParams + "<NSLEnabled>1</NSLEnabled>";
		}

		xmlParams = xmlParams +
			"<SecurityValue>" + security + "</SecurityValue>";

		if (!WorkDir.equals(""))
		{
			xmlParams = xmlParams + "<WorkDir>" + WorkDir + "</WorkDir>";
		}

		if (!clientName.equals(""))
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

		if (CacheQLen <= 0)
		{
			CacheQLen = 25;
		}

		xmlParams = xmlParams + "<CacheQLen>" + CacheQLen + "</CacheQLen>";

		if ((NetThMin <= 0) || (NetThMin > NetThMax))
		{
			NetThMin = 10;
		}

		xmlParams = xmlParams + "<SoapThMin>" + NetThMin + "</SoapThMin>";

		if ((NetThMax <= 0) || (NetThMax < NetThMin))
		{
			NetThMax = 10;
		}

		xmlParams = xmlParams + "<SoapThMax>" + NetThMax + "</SoapThMax>";

		if ((OfflineMode != 0) && (OfflineMode != 1))
		{
			OfflineMode = 0;
		}

		xmlParams = xmlParams + "<OfflineMode>" + OfflineMode + "</OfflineMode>";

		if (!ProxyIP.equals(""))
		{
			xmlParams = xmlParams + "<ProxyIP>" + ProxyIP + "</ProxyIP>";
		}

		if (!ProxyPort.equals(""))
		{
			xmlParams = xmlParams + "<ProxyPort>" + ProxyPort + "</ProxyPort>";
		}

		if (!ProxyUsername.equals(""))
		{
			xmlParams = xmlParams +
				"<ProxyUsername>" + ProxyUsername+ "</ProxyUsername>";
		}

		if (!ProxyPass.equals(""))
		{
			xmlParams = xmlParams +
				"<ProxyPassword>" + ProxyPass + "</ProxyPassword>";
		}

		if (!DaemonIP.equals(""))
		{
			xmlParams = xmlParams +
				"<DaemonIP>" + DaemonIP + "</DaemonIP>";
		}

		if (!DaemonPort.equals(""))
		{
			xmlParams = xmlParams +
				"<DaemonPort>" + DaemonPort + "</DaemonPort>";
		}

		if (!DaemonUser.equals(""))
		{
			xmlParams = xmlParams +
				"<DaemonUser>" + DaemonUser + "</DaemonUser>";
		}

		if (!DaemonPass.equals(""))
		{
			xmlParams = xmlParams +
				"<DaemonPass>" + DaemonPass + "</DaemonPass>";
		}

		xmlParams = xmlParams + "</SHAFERXMLParams>";

		try
		{
			i = NalpLibOpen((xmlParams + '\000').getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0)
		{
			log.error("Error {}: {}", i, callNalpGetErrorMsg(i));
		}

		return i;
	}


/**
 * @brief Shuts down the library. THIS FUNCTION MUST BE CALLED IMMEDIATELY
 * BEFORE CLOSING THE LIBRARY (ie with dlclose). If this function is not
 * called before the library is closed, information may be lost and memory
 * corruption could occur.
 *
 * @throws NalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 *
 * @return = 0 If the call succeeded
 *
 * @throws NalpError:   If there was a problem calling the NSL function,
 *  this error will be thrown. (\ref V10ERROR)
 */
	public int
	callNalpLibClose()
	throws NalpError
	{
		int	i;

		i = NalpLibClose();

		if (i < 0)
		{
			throw new NalpError(i, NalpGetErrorMsg(i));
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
	callNalpGetErrorMsg(int nalpErrorNo)
	throws NalpError
	{
		String 	nalpErrorMsg;


		nalpErrorMsg = NalpGetErrorMsg(nalpErrorNo);

		return new String(nalpErrorMsg);
	}
}

/** @} */ //end of NALPJAVA

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
