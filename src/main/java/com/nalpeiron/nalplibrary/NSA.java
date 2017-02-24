//
// nsa.java
//
// Created Jan 19 2012
// R. D. Ramey
//
// based on NLS version by Jeremy Porath
// Created April 26, 2010

package com.nalpeiron.nalplibrary;

import java.io.UnsupportedEncodingException;

public class NSA
{
	private NALP nalp;

	private native int NSAGetVersion(long libHandle, byte[] nsaVersion);

	private native int NSAGetHostName(long libHandle, byte[] nsaHostName);

	private native int NSALogin(long LibHandle,
			byte[] Username, byte[] clientData, long[] transID);

	private native int NSALogout(long LibHandle,
			byte[] Username, byte[] clientData, long[] transID);

	private native int NSAFeatureStart(long LibHandle, byte[] Username,
			byte[] FeatureCode, byte[] clientData, long[] transID);

	private native int NSAFeatureStop(long LibHandle, byte[] Username,
			byte[] FeatureCode, byte[] clientData, long[] transID);

	private native int NSAException(long LibHandle,
			byte[] Username, byte[] ExceptionCode,
			byte[] Description, byte[] clientData, long[] transID);

	private native int NSASysInfo(long LibHandle,
			byte[] Username, byte[] Applang, byte[] Version,
			byte[] Edition, byte[] Build, byte[] LicenseStat,
			byte[] clientData, long[] transID);

	private native int NSASendCache(long LibHandle,
			byte[] Username, long[] transID);

	private native int NSAApStart(long LibHandle,
			byte[] clientData, long[] transID);

	private native int NSAApStop(long LibHandle,
			byte[] clientData, long[] transID);

	private native int NSAGetLocation(long LibHandle);

	private native int NSAGetPrivacy(long LibHandle);

	private native int NSASetPrivacy(long LibHandle, int Privacy);

	private native int NSAGetStats(long LibHandle, byte[] nsaStats);


	//Use this so we don't have to keep track of the library or
	// its handle.
	public NSA(NALP nalp)
	{   
		this.nalp = nalp;
	}


/**
 * Call NSAGetVersion() 
 * @return:	0 on success, negative value on error, nsaVersion
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public String
	callNSAGetVersion()
	throws nalpError
	{
		byte[]	nsaVersion = new byte[256];
		int		i;


		i = NSAGetVersion(nalp.LibHandle, nsaVersion);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron's library uses UTF-8 internally
		try 
		{
			return new String(nsaVersion, "UTF-8");
		} 
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}


/**
 * Call NSAGetHostName()
 * @return:	0 on success, negative value on error, hostname
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public String
	callNSAGetHostName()
	throws nalpError
	{
		byte[]	nsaHostName = new byte[128];
		int		i;


		i = NSAGetHostName(nalp.LibHandle, nsaHostName);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron's library uses UTF-8 internally
		try 
		{
			return new String(nsaHostName, "UTF-8");
		} 
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}


/**
 * Call NSALogin() 
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSALogin(String Username, String clientData, long[] lid)
	throws nalpError
	{
		int	i;


		try 
		{
			i = NSALogin(nalp.LibHandle,
					(Username + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), lid);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSALogout()
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSALogout(String Username, String clientData, long[] lid)
	throws nalpError
	{
		int	i;


		try
		{
			i = NSALogout(nalp.LibHandle,
					(Username + '\000').getBytes("UTF-8"),
					(clientData + '\000').getBytes("UTF-8"), lid);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSAFeatureStart()
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSAFeatureStart(String Username,
			String FeatureCode, String clientData, long[] fid)
	throws nalpError
	{
		int	i;


		try
		{
			i = NSAFeatureStart(nalp.LibHandle,
				(Username + '\000').getBytes("UTF-8"),
				(FeatureCode + '\000').getBytes("UTF-8"),
				(clientData + '\000').getBytes("UTF-8"), fid);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSAFeatureStop()
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSAFeatureStop(String Username,
			String FeatureCode, String clientData, long[] fid)
	throws nalpError
	{
		int	i;


		try
		{
			i = NSAFeatureStop(nalp.LibHandle,
				(Username + '\000').getBytes("UTF-8"), 
				(FeatureCode + '\000').getBytes("UTF-8"), 
				(clientData + '\000').getBytes("UTF-8"), fid);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSAException
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSAException(String Username,
			String ExceptionCode, String clientData, String Description)
	throws nalpError
	{
		int	i;
		long[] transID = new long[1];


		//-1 disables grouping of transactions for this call
		transID[0] = -1;

		try
		{
			i = NSAException(nalp.LibHandle,
				(Username + '\000').getBytes("UTF-8"),
				(ExceptionCode + '\000').getBytes("UTF-8"),
				(Description + '\000').getBytes("UTF-8"),
				(clientData + '\000').getBytes("UTF-8"), transID);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSASysInfo() - Collect and send system information to Napeiron
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSASysInfo(String Username, String Applang, String Version,
			String Edition, String Build, String LicenseStat, String clientData)
	throws nalpError
	{
		int	i;
		long[] transID = new long[1];


		//-1 disables grouping of transactions for this call
		transID[0] = -1;

		try
		{
			i = NSASysInfo(nalp.LibHandle,
				(Username + '\000').getBytes("UTF-8"),
				(Applang + '\000').getBytes("UTF-8"),
				(Version + '\000').getBytes("UTF-8"),
				(Edition + '\000').getBytes("UTF-8"),
				(Build + '\000').getBytes("UTF-8"),
				(LicenseStat + '\000').getBytes("UTF-8"),
				(clientData + '\000').getBytes("UTF-8"), transID);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSASendCache() -- Send any cache file to Nalpeiron server
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSASendCache(String Username)
	throws nalpError
	{
		int	i;
		long[] transID = new long[1];


		//-1 disables grouping of transactions for this call
		transID[0] = -1;

		try
		{
			i = NSASendCache(nalp.LibHandle,
				(Username + '\000').getBytes("UTF-8"), transID);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSAApStart()
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSAApStart(String clientData, long[] aid)
	throws nalpError
	{
		int	i;
		
		
		try
		{
			i = NSAApStart(nalp.LibHandle,
					(clientData + '\000').getBytes("UTF-8"), aid);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSAApStop()
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSAApStop(String clientData, long[] aid)
	throws nalpError
	{
		int	i;


		try
		{
			i = NSAApStop(nalp.LibHandle,
					(clientData + '\000').getBytes("UTF-8"), aid);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSAGetLocation() -- Get location data from Nalpeiron server
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSAGetLocation()
	throws nalpError
	{
		int	i;


		i = NSAGetLocation(nalp.LibHandle);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSAGetPrivacy() Get the privacy setting
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSAGetPrivacy()
	throws nalpError
	{
		int	i;

		i = NSAGetPrivacy(nalp.LibHandle);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSASetPrivacy() set privacy value (0 off or 1 on)
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSA function,
 * 	this error will be thrown
 */
	public int
	callNSASetPrivacy(int Privacy)
	throws nalpError
	{
		int	i;


		i = NSASetPrivacy(nalp.LibHandle, Privacy);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSAGetStats(), return performance information from NSA library
 * @return: utf8 xml string containing information
 */
	public String
	callNSAGetStats()
	throws nalpError
	{
		byte[] 	nsaStats = new byte[1024];
		int		i;
	

		i = NSAGetStats(nalp.LibHandle, nsaStats);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron's library uses UTF-8 internally
		try 
		{
			return new String(nsaStats, "UTF-8");
		} 
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}
}


/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
