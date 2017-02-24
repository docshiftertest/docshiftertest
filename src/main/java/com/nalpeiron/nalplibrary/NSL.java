//
// nsl.java
//
// Created July 19 2012
// R. D. Ramey
//

package com.nalpeiron.nalplibrary;

import java.io.UnsupportedEncodingException;

public class NSL 
{
	private NALP nalp;

	//Security offset. Set it in constructor so we can adjust
	// returns from NSL functions
	private int offset;

	private native int NSLValidateLibrary(long LibHandle,
				long custID, long prodID, int offset);

	private native int NSLGetVersion(long LibHandle,
				byte[] nslVersion, int offset);

	private native int NSLGetComputerID(long LibHandle,
				byte[] computerID, int offset);

	private native int NSLGetHostName(long LibHandle,
				byte[] hostName, int offset);

	private native int NSLGetLeaseExpDate(long LibHandle,
				byte[] expDate, int offset);

	private native int NSLGetLeaseExpSec(long LibHandle,
				int[] expSec, int[] expEpoch, int offset);

	private native int NSLGetMaintExpDate(long LibHandle,
				byte[] expDate, int offset);

	private native int NSLGetMaintExpSec(long LibHandle,
				int[] expSec, int[] expEpoch, int offset);

	private native int NSLGetSubExpDate(long LibHandle,
				byte[] expDate, int offset);

	private native int NSLGetSubExpSec(long LibHandle,
				int[] expSec, int[] expEpoch, int offset);

	private native int NSLGetTrialExpDate(long LibHandle,
				byte[] expDate, int offset);

	private native int NSLGetTrialExpSec(long LibHandle,
				int[] expSec, int[] expEpoch, int offset);

	private native int NSLGetLicenseCode(long LibHandle,
				byte[] licCode, int offset);

	private native int NSLGetLicenseStatus(long LibHandle,
				int[] licStat, int offset);

	private native int NSLGetTimeStamp(long LibHandle,
				int[] timeStamp, int offset);

	private native int NSLGetFeatureStatus(long LibHandle,
				byte[] featureName, int[] fStat, int offset);

	private native int NSLGetLicense(long LibHandle,
			byte[] licenseNo, byte[] xmlRegInfo, int[] licStat, int offset);

	private native int NSLReturnLicense(long LibHandle,
			byte[] licenseNo, int[] licStat, int offset);

	private native int NSLImportCertificate(long LibHandle,
					byte[] cert, byte[] licenseNo, int[] licStat, int offset);

	private native int NSLGetActivationCertReq(long LibHandle,
					byte[] licenseNo, byte[] xmlRegInfo, byte[] cert, int offset);

	private native int NSLGetDeactivationCertReq(long LibHandle,
					byte[] licenseNo, byte[] cert, int offset);

	private native int NSLGetUDFValue(long LibHandle,
			byte[] UDFName, byte[] UDFValue, int offset);

	private native int NSLGetNumbAvailProc(long LibHandle,
				int[] maxProc, int[] availProc, int offset);

	private native int NSLRegister(long LibHandle, byte[] licenseNo,
				byte[] xmlRegInfo, int offset);

	private native int NSLTestConnection(long LibHandle, int offset);
	//Use this so we don't have to keep track of the library or
	// its handle.
	
	public NSL(NALP nalp, int oset)
	{
		this.nalp = nalp;

		offset = oset;
	}

/**
 * Call NSLGetVersion() 
 * @return:	0 on success, negative value on error, nslVersion
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetVersion()
	throws nalpError
	{
		byte[]	nslVersion = new byte[256];
		int		i;


		i = NSLGetVersion(nalp.LibHandle, nslVersion, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslVersion, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * Call NSLGetComputerID()
 * @return:	0 on success, negative value on error, computerID
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetComputerID()
	throws nalpError
	{
		byte[]	nslCompID = new byte[50];
		int		i;


		i = NSLGetComputerID(nalp.LibHandle, nslCompID, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslCompID, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * Call NSLGetHostName()
 * @return:	0 on success, negative value on error, hostname
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetHostName()
	throws nalpError
	{
		byte[]	nslHostName = new byte[128];
		int		i;


		i = NSLGetHostName(nalp.LibHandle, nslHostName, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslHostName, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * Call NSLGetLeaseExpSec() -- Send any cache file to Nalpeiron server
 * @return:	0 on success, negative value on error, exp epoch seconds
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLGetLeaseExpSec()
	throws nalpError
	{
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int	i;


		i = NSLGetLeaseExpSec(nalp.LibHandle, expSec, expEpoch, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return expSec[0];
	}

/**
 * Call NSLGetLeaseExpDate()
 * @return:	0 on success, negative value on error, Expiration date
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetLeaseExpDate()
	throws nalpError
	{
		byte[]	nslExpDate = new byte[128];
		int		i;


		i = NSLGetLeaseExpDate(nalp.LibHandle, nslExpDate, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslExpDate, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * Call NSLGetMaintExpSec() -- Send any cache file to Nalpeiron server
 * @return:	0 on success, negative value on error, exp epoch seconds
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLGetMaintExpSec()
	throws nalpError
	{
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int	i;


		i = NSLGetMaintExpSec(nalp.LibHandle, expSec, expEpoch, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return expSec[0];
	}

/**
 * Call NSLGetMaintExpDate()
 * @return:	0 on success, negative value on error, Expiration date
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetMaintExpDate()
	throws nalpError
	{
		byte[]	nslExpDate = new byte[128];
		int		i;


		i = NSLGetMaintExpDate(nalp.LibHandle, nslExpDate, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslExpDate, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}


/**
 * Call NSLGetSubExpSec() -- Send any cache file to Nalpeiron server
 * @return:	0 on success, negative value on error, exp epoch seconds
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLGetSubExpSec()
	throws nalpError
	{
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int	i;


		i = NSLGetSubExpSec(nalp.LibHandle, expSec, expEpoch, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return expSec[0];
	}

/**
 * Call NSLGetSubExpDate()
 * @return:	0 on success, negative value on error, Expiration date
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetSubExpDate()
	throws nalpError
	{
		byte[]	nslExpDate = new byte[128];
		int		i;


		i = NSLGetSubExpDate(nalp.LibHandle, nslExpDate, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslExpDate, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}


/**
 * Call NSLGetTrialExpSec() -- Send any cache file to Nalpeiron server
 * @return:	0 on success, negative value on error, exp epoch seconds
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLGetTrialExpSec()
	throws nalpError
	{
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int	i;


		i = NSLGetTrialExpSec(nalp.LibHandle, expSec, expEpoch, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return expSec[0];
	}

/**
 * Call NSLGetTrialExpDate()
 * @return:	0 on success, negative value on error, Expiration date
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetTrialExpDate()
	throws nalpError
	{
		byte[]	nslExpDate = new byte[128];
		int		i;


		i = NSLGetTrialExpDate(nalp.LibHandle, nslExpDate, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslExpDate, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * Call NSLGetLicense 
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLGetLicense(String licenseNo, String xmlRegInfo)
	throws nalpError
	{
		int[] licStat = new int[1];
		int	i;


		try
		{
			i = NSLGetLicense(nalp.LibHandle,
				(licenseNo + '\000').getBytes("UTF-8"),
				(xmlRegInfo + '\000').getBytes("UTF-8"),
				licStat, offset);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return licStat[0];
	}

/**
 * Call NSLReturnLicense 
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLReturnLicense(String licenseNo)
	throws nalpError
	{
		int[] licStat = new int[1];
		int	i;


		try
		{
			i = NSLReturnLicense(nalp.LibHandle,
				(licenseNo + '\000').getBytes("UTF-8"), licStat, offset);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return licStat[0];
	}

/**
 * Call NSLImportCertificate
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLImportCertificate(String licenseNo, String cert)
	throws nalpError
	{
		int[] licStat = new int[1];
		int	i;

		try
		{
			i = NSLImportCertificate(nalp.LibHandle,
				(cert + '\000').getBytes("UTF-8"),
				(licenseNo + '\000').getBytes("UTF-8"), licStat, offset);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return licStat[0];
	}

/**
 * Call NSLGetActivationCertReq() 
 * @return:	0 on success, negative value on error, nslVersion
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetActivationCertReq(String licenseNo, String xmlRegInfo)
	throws nalpError
	{
		byte[]	nslCert = new byte[4096];
		int		i;


		try
		{
			i = NSLGetActivationCertReq(nalp.LibHandle,
				(licenseNo + '\000').getBytes("UTF-8"),
				(xmlRegInfo + '\000').getBytes("UTF-8"), nslCert, offset);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslCert, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}


/**
 * Call NSLGetDeactivationCertReq() 
 * @return:	0 on success, negative value on error, nslVersion
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetDeactivationCertReq(String licenseNo)
	throws nalpError
	{
		byte[]	nslCert = new byte[4096];
		int		i;


		try
		{
			i = NSLGetDeactivationCertReq(nalp.LibHandle,
				(licenseNo + '\000').getBytes("UTF-8"), nslCert, offset);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslCert, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}


/**
 * Call NSLValidateLibrary() - Collect and send system information to Napeiron
 * @return:	0 on success, anything else in an invalid library
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLValidateLibrary(int custID, int prodID)
	throws nalpError
	{
		int	i;


		i = NSLValidateLibrary(nalp.LibHandle, custID, prodID, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSLGetLicenseStatus()
 * @return:	0 on success, negative value on error, license status
 * @throws nalpError:	If there was a problem calling the NSLfunction,
 * 	this error will be thrown
 */
	public int callNSLGetLicenseStatus()
	throws nalpError
	{
		int[] licStat = new int[1];
		int	i;


		i = NSLGetLicenseStatus(nalp.LibHandle, licStat, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return licStat[0];
	}

/**
 * Call NSLGetLicenseCode()
 * @return:	0 on success, negative value on error, license code 
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String callNSLGetLicenseCode()
	throws nalpError
	{
		byte[]	nslLicCode = new byte[256];
		int	i;


		i = NSLGetLicenseCode(nalp.LibHandle, nslLicCode, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(nslLicCode, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * Call NSLGetTimeStamp()
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLGetTimeStamp()
	throws nalpError
	{
		int[] timeStamp = new int[1];
		int	i;


		i = NSLGetTimeStamp(nalp.LibHandle, timeStamp, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return timeStamp[0];
	}

/**
 * Call NSLGetFeatureStatus() -- Get location data from Nalpeiron server
 * @return:	0 on success, negative value on error, feature status
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int callNSLGetFeatureStatus(String featureName)
	throws nalpError
	{
		int[] featureStatus = new int[1];
		int	i;
		
		
		try
		{
			i = NSLGetFeatureStatus(nalp.LibHandle,
				(featureName + '\000').getBytes("UTF-8"),
				featureStatus, offset);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return featureStatus[0];
	}

/**
 * Call NSLGetUDFValue()
 * @return:	0 on success, negative value on error, Expiration date
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public String
	callNSLGetUDFValue(String UDFName)
	throws nalpError
	{
		byte[]	UDFValue = new byte[4096];
		int		i;

		
		try
		{
			i = NSLGetUDFValue(nalp.LibHandle,
				(UDFName + '\000').getBytes("UTF-8"),
				UDFValue, offset);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(UDFValue, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}

/*	
	public String
	callNSLGetUDFValue(String UDFName)
	throws nalpError
	{
		byte[]	UDFValue = new byte[4096];
		int		i;

		
		try
		{
			i = NSLGetUDFValue(nalp.LibHandle,
				(UDFName + '\000').getBytes("UTF-8"), UDFValue, offset);
		}
		catch (UnsupportedEncodingException e) 
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
		
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalpeiron library uses UTF-8 internally
		try
		{
			return new String(UDFValue, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new nalpError(-9006, "Invalid Encoding");
		}
	}
*/

/**
 * Call NSLGetNumbAvailProc()
 * @return:	0 on success, negative value on error, license status
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int
	callNSLGetNumbAvailProc(int[] maxProc, int[] availProc)
	throws nalpError
	{
		int	i;


		i = NSLGetNumbAvailProc(nalp.LibHandle, maxProc, availProc, offset);

		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

/**
 * Call NSLRegister
 * @return:	0 on success, negative value on error
 * @throws nalpError:	If there was a problem calling the NSL function,
 * 	this error will be thrown
 */
	public int callNSLRegister(String licenseNo, String xmlRegInfo)
	throws nalpError
	{
		int	i;
		
		
		try
		{
			i = NSLRegister(nalp.LibHandle,
				(licenseNo + '\000').getBytes("UTF-8"),
				(xmlRegInfo + '\000').getBytes("UTF-8"), offset);
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
	
	public int callNSLTestConnection()
	throws nalpError
	{
		int	i;
		i = NSLTestConnection(nalp.LibHandle, offset);
		if (i < 0)
		{
			throw new nalpError(i, nalp.callNalpGetErrorMsg(i));
		}
		return i;
	}
}

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
