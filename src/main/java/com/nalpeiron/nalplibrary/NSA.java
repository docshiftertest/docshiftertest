//
// nsa.java
//
// Created Jan 19 2012
// R. D. Ramey

package com.nalpeiron.nalplibrary;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Generic and initialization functions for Nalpeiron V10 library
 */
public class NSA {
	private final NALP nalp;

	private native int NSAGetVersion(byte[] nsaVersion);

	private native int NSAGetHostName(byte[] nsaHostName);

	private native int NSALogin(byte[] Username, byte[] clientData, long[] transID);

	private native int NSALogout(byte[] Username, byte[] clientData, long[] transID);

	private native int NSAFeatureStart(byte[] Username,
									   byte[] FeatureCode, byte[] clientData, long[] transID);

	private native int NSAFeatureStop(byte[] Username,
									  byte[] FeatureCode, byte[] clientData, long[] transID);

	private native int NSAException(byte[] Username, byte[] ExceptionCode,
									byte[] Description, byte[] clientData, long[] transID);

	private native int NSASysInfo(byte[] Username, byte[] Applang, byte[] Version,
								  byte[] Edition, byte[] Build, byte[] LicenseStat,
								  byte[] clientData, long[] transID);

	private native int NSASendCache(byte[] Username, long[] transID);

	private native int NSAApStart(byte[] Username, byte[] clientData, long[] transID);

	private native int NSAApStop(byte[] Username, byte[] clientData, long[] transID);

	private native int NSAGetPrivacy();

	private native int NSASetPrivacy(int Privacy);

	private native int NSAGetStats(byte[] nsaStats);


	public NSA(
			NALP nalp
	) {
		this.nalp = nalp;
	}

	/**
	 * Gets the version of the NSA library being accessed.  See NSAGetVersion().
	 *
	 * @return A string containing the library version.
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public String callNSAGetVersion() throws NalpError {
		byte[] nsaVersion = new byte[256];
		int i;


		i = NSAGetVersion(nsaVersion);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp's library uses UTF-8 internally
		return new String(nsaVersion, 0, i, StandardCharsets.UTF_8);
	}


	/**
	 * Gets the name of the SOAP server the library contacts for
	 * licensing information. See NSAGetHostName().
	 *
	 * @return A string containing the hostname of the Nalpeiron server or
	 * daemon.
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public String
	callNSAGetHostName()
			throws NalpError {
		byte[] nsaHostName = new byte[128];
		int i;


		i = NSAGetHostName(nsaHostName);

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		//Nalp's library uses UTF-8 internally
		try {
			return new String(nsaHostName, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}


	/**
	 * Records login of username.  See NSALogin().
	 *
	 * @param Username a string containing the username that
	 * will be passed to the Nalpeiron server or daemon with your call.
	 *
	 * @param clientData a string containing a valid XML fragment
	 * with whatever data you care to pass to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param lid transID allows a series of transactions to be grouped
	 * together with a transaction ID.  To retrieve a transaction ID from any
	 * function, set transID = 0 and call the function. Upon return, transID
	 * will be set with a random number that will be passed to the Nalpeiron
	 * server to identify the transaction.  Send this value into any functions
	 * that are to be grouped together.  This is particularly useful for
	 * function pairs such as NSALogin/NSALogout, NSAAppStart/NSAAppStop,
	 * NSAFeatureStart/NSAFeatureEnd, etc.  If you do not wish to use the
	 * transaction ID a NULL pointer will disable it.
	 *
	 * @return 0 If the call succeeded
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSALogin(String Username, String clientData, long[] lid)
			throws NalpError {
		int i;


		try {
			i = NSALogin((Username + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), lid);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * Records login of username.  See NSALogout().
	 *
	 * @param Username a string containing the username that
	 * will be passed to the Nalpeiron server or daemon with your call.
	 *
	 * @param clientData a string containing a valid XML fragment
	 * with whatever data you care to pass to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param lid transID allows a series of transactions to be grouped
	 * together with a transaction ID.  To retrieve a transaction ID from any
	 * function, set transID = 0 and call the function. Upon return, transID
	 * will be set with a random number that will be passed to the Nalpeiron
	 * server to identify the transaction.  Send this value into any functions
	 * that are to be grouped together.  This is particularly useful for
	 * function pairs such as NSALogin/NSALogout, NSAAppStart/NSAAppStop,
	 * NSAFeatureStart/NSAFeatureEnd, etc.  If you do not wish to use the
	 * transaction ID a NULL pointer will disable it.
	 *
	 * @return 0 If the call succeeded
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSALogout(String Username, String clientData, long[] lid)
			throws NalpError {
		int i;


		try {
			i = NSALogout((Username + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), lid);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * Records start of feature access.  See NSAFeatureStart().
	 *
	 * @param Username a string containing the username that
	 * will be passed to the Nalpeiron server or daemon with your call.
	 *
	 * @param FeatureCode a string containing the the five(5) character
	 * feature code of the feature to be accessed
	 *
	 * @param clientData a string containing a valid XML fragment
	 * with whatever data you care to pass to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param fid transID allows a series of transactions to be grouped
	 * together with a transaction ID.  To retrieve a transaction ID from any
	 * function, set transID = 0 and call the function. Upon return, transID
	 * will be set with a random number that will be passed to the Nalpeiron
	 * server to identify the transaction.  Send this value into any functions
	 * that are to be grouped together.  This is particularly useful for
	 * function pairs such as NSALogin/NSALogout, NSAAppStart/NSAAppStop,
	 * NSAFeatureStart/NSAFeatureEnd, etc.  If you do not wish to use the
	 * transaction ID a NULL pointer will disable it.
	 *
	 * @return 0 If the call succeeded
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSAFeatureStart(String Username,
						String FeatureCode, String clientData, long[] fid)
			throws NalpError {
		int i;


		try {
			i = NSAFeatureStart((Username + '\000').getBytes("UTF-8"),
					(FeatureCode + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), fid);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * Records end of feature access.  See NSAFeatureEnd().
	 *
	 * @param Username a string containing the username that
	 * will be passed to the Nalpeiron server or daemon with your call.
	 *
	 * @param FeatureCode a string containing the the five(5) character
	 * feature code of the feature to be accessed
	 *
	 * @param clientData a string containing a valid XML fragment
	 * with whatever data you care to pass to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param fid transID allows a series of transactions to be grouped
	 * together with a transaction ID.  To retrieve a transaction ID from any
	 * function, set transID = 0 and call the function. Upon return, transID
	 * will be set with a random number that will be passed to the Nalpeiron
	 * server to identify the transaction.  Send this value into any functions
	 * that are to be grouped together.  This is particularly useful for
	 * function pairs such as NSALogin/NSALogout, NSAAppStart/NSAAppStop,
	 * NSAFeatureStart/NSAFeatureEnd, etc.  If you do not wish to use the
	 * transaction ID a NULL pointer will disable it.
	 *
	 * @return 0 If the call succeeded
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSAFeatureStop(String Username,
					   String FeatureCode, String clientData, long[] fid)
			throws NalpError {
		int i;


		try {
			i = NSAFeatureStop((Username + '\000').getBytes("UTF-8"),
					(FeatureCode + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), fid);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * Records an error of type exceptionCode by username
	 * with details in description.  See NSAException().
	 *
	 * @param Username a string containing the username that
	 * will be passed to the Nalpeiron server or daemon with your call.
	 *
	 * @param ExceptionCode a string that will be passed to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param Description A string that will be passed to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param clientData a XML fragment containing whatever data you
	 * would like to pass to the Nalpeiron server or daemon with your call.
	 *
	 * @return 0 If the call succeeded
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSAException(String Username,
					 String ExceptionCode, String clientData, String Description)
			throws NalpError {
		int i;
		long[] transID = new long[1];


		//-1 disables grouping of transactions for this call
		transID[0] = -1;

		try {
			i = NSAException((Username + '\000').getBytes("UTF-8"),
					(ExceptionCode + '\000').getBytes("UTF-8"),
					(Description + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), transID);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
		}

		return i;
	}

	/**
	 * Sends information about the end user's system to Nalpeiron. 
	 * See NSASysInfo(). In addtion to the parameters the function will send
	 * the following information
	 *
	 * @verbatim date            date and time
	username        username as passed into NSASysInfo
	productid       your product ID
	customerid      your customer ID
	computerid      the end user's computer ID
	NSA version     version number of the NSA Library
	location        countryid- Two character country code
	country         country name
	region          state, territory, region as appropriate
	city            city
	zipcode         zipcode as appropriate
	operatingsystem OS (Linux, Windows, OSX, etc)
	bitage          32 or 64
	screenresx      screen width
	screenresy      screen heigth
	proc            processor information
	cores           number of processors or cores
	memory          amount of memory in Meg.
	oslanguage      default OS language
	applanguage     passed in app language
	productstatus   passed in License Status
	profile         type of system(laptop or desktop)
	 @endverbatim
	  *
	  * @param Username a username that will be passed to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param Applang a string that will be passed to the Nalpeiron server
	 * or daemon with your call.
	 *
	 * @param Version a string that will be passed to the Nalpeiron server
	 * or daemon with your call.
	 *
	 * @param Edition a string that will be passed to the Nalpeiron server
	 * or daemon with your call.
	 *
	 * @param Build a string that will be passed to the Nalpeiron server or
	 * daemon with your call.
	 *
	 * @param LicenseStat a string that will be passed to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param clientData a string containing a valid XML fragment
	 * containing whatever data you would like to pass to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @return 0 on success, negative value on error
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSASysInfo(String Username, String Applang, String Version,
				   String Edition, String Build, String LicenseStat, String clientData)
			throws NalpError {
		int i;
		long[] transID = new long[1];


		//-1 disables grouping of transactions for this call
		transID[0] = -1;

		try {
			i = NSASysInfo((Username + '\000').getBytes("UTF-8"),
					(Applang + '\000').getBytes("UTF-8"),
					(Version + '\000').getBytes("UTF-8"),
					(Edition + '\000').getBytes("UTF-8"),
					(Build + '\000').getBytes("UTF-8"),
					(LicenseStat + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), transID);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * If a cache file of analytics data exists on the end user's
	 * system send it to Nalpeiron.  Cache files are created when a
	 * system is offline or has been placed in offline mode. See NSASendCache().
	 *
	 * @param Username a username that will be passed to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @return 0 If the call succeeded
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSASendCache(String Username)
			throws NalpError {
		int i;
		long[] transID = new long[1];


		//-1 disables grouping of transactions for this call
		transID[0] = -1;

		try {
			i = NSASendCache((Username + '\000').getBytes("UTF-8"), transID);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * Records start of application.  See NSAAppStart().
	 *
	 * @param Username a string containing the username that
	 * will be passed to the Nalpeiron server or daemon with your call.
	 *
	 * @param clientData a string containing a valid XML fragment
	 * with whatever data you care to pass to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param aid transID allows a series of transactions to be grouped
	 * together with a transaction ID.  To retrieve a transaction ID from any
	 * function, set transID = 0 and call the function. Upon return, transID
	 * will be set with a random number that will be passed to the Nalpeiron
	 * server to identify the transaction.  Send this value into any functions
	 * that are to be grouped together.  This is particularly useful for
	 * function pairs such as NSALogin/NSALogout, NSAAppStart/NSAAppStop,
	 * NSAFeatureStart/NSAFeatureEnd, etc.  If you do not wish to use the
	 * transaction ID a NULL pointer will disable it.
	 *
	 * @return 0 If the call succeeded
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSAApStart(String Username, String clientData, long[] aid)
			throws NalpError {
		int i;


		try {
			i = NSAApStart((Username + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), aid);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * Records end of application.  See NSAAppStop().
	 *
	 * @param Username a string containing the username that
	 * will be passed to the Nalpeiron server or daemon with your call.
	 *
	 * @param clientData a string containing a valid XML fragment
	 * with whatever data you care to pass to the Nalpeiron
	 * server or daemon with your call.
	 *
	 * @param aid transID allows a series of transactions to be grouped
	 * together with a transaction ID.  To retrieve a transaction ID from any
	 * function, set transID = 0 and call the function. Upon return, transID
	 * will be set with a random number that will be passed to the Nalpeiron
	 * server to identify the transaction.  Send this value into any functions
	 * that are to be grouped together.  This is particularly useful for
	 * function pairs such as NSALogin/NSALogout, NSAAppStart/NSAAppStop,
	 * NSAFeatureStart/NSAFeatureEnd, etc.  If you do not wish to use the
	 * transaction ID a NULL pointer will disable it.
	 *
	 * @return 0 If the call succeeded
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSAApStop(String Username, String clientData, long[] aid)
			throws NalpError {
		int i;


		try {
			i = NSAApStop((Username + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), aid);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * Checks the current privacy setting.  If privacy is unset, the
	 * privacy setting used by the library will be the one stamped into it.
	 * See NSAGetPrivacy().
	 *
	 * @return 0 No privacy
	 * @return 1 Privacy Enabled
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSAGetPrivacy()
			throws NalpError {
		int i;

		i = NSAGetPrivacy();

		if (i < 0) {
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * Sets the library's privacy setting to nsaPriv.  See NSASetPrivacy().
	 *
	 * @param Privacy privacy value for the library. Possible values for setting
	 * are 0 "no privacy" or 1 "privacy enabled".
	 *
	 * @return 0 If the call succeeded
	 *
	 * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public int
	callNSASetPrivacy(int Privacy)
			throws NalpError {
		int i;


		i = NSASetPrivacy(Privacy);

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * Returns status information from the NSA library. See NSAGetStats().
	 *
	 * @return a string containing statistics from the NSA library as a XML string.
	 * The format of the return is:
	 *@verbatim
	<?xml version="1.0" encoding="UTF-8"?>
	<NSA Current Statistics>
	<OfflineMode>0</OfflineMode>
	<LocationInfo>1</LocationInfo>
	<Cache Thread>
	<Max Cache Que Len>25</Max Cache Que Len>
	<Current Cache Que Len>0</Current Cache Que Len>
	<Cache writes>1</Cache writes>
	<Cache fails>0</Cache fails>
	</Cache Thread>
	<Soap Pool>
	<Min Soap Threads>10</Min Soap Threads>
	<Max Soap Threads>10</Max Soap Threads>
	<Queued Jobs>0</Queued Jobs>
	<Running Jobs>0</Running Jobs>
	<Finished Jobs>145</Finished Jobs>
	</Soap Pool>
	</NSA Current Statistics>
	 @endverbatim
	  *
	  * @throws NalpError:	If there was a problem calling the NSA function,
	 * 	this error will be thrown (\ref V10ERROR)
	 */
	public String
	callNSAGetStats()
			throws NalpError {
		byte[] nsaStats = new byte[1024];
		int i;


		i = NSAGetStats(nsaStats);

		if (i < 0) {
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i), "quiet");
			System.out.println("Error " + i + ": " + nalp.callNalpGetErrorMsg(i));
		}

		//Nalp's library uses UTF-8 internally
		try {
			return new String(nsaStats, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}
}

//end of NALPJAVA

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
