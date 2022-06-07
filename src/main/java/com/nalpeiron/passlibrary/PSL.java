//
// psl.java
//
// Created July 19 2012
// R. D. Ramey
//


/**
 *  @defgroup PSLJava Nalpeiron Passive Licensing Java classes
 *  @ingroup PASSIVE
 *  @ingroup PASSIVEJAVA
 *  @{
 */

/**
 *  @file   PSL.java
 *  @brief  Generic and initialization functions for Nalpeiron Passive library
 */

package com.nalpeiron.passlibrary;

import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import com.nalpeiron.NalpError;
import lombok.extern.log4j.Log4j2;

import java.io.UnsupportedEncodingException;

@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
public class PSL
{
	private NALP nalp;

	//Security offset. Set it in constructor so we can adjust
	// returns from PSL functions
	private int offset;

	private String	licenseNo;


	private native int PSLValidateLibrary(long custID, long prodID, int offset);

	private native int PSLGetVersion(byte[] pslVersion, int offset);

	private native int PSLGetLibraryOptions(long[] libOptions, int offset);

	private native int PSLGetComputerID(byte[] computerID, int offset);

	private native int PSLGetHostName(byte[] hostName, int offset);

	private native int PSLGetLeaseExpDate(byte[] expDate, int offset);

	private native int PSLGetLeaseExpSec(int[] expSec, int[] expEpoch, int offset);

	private native int PSLGetMaintExpDate(byte[] expDate, int offset);

	private native int PSLGetMaintExpSec(int[] expSec, int[] expEpoch, int offset);

	private native int PSLGetLicenseCode(byte[] licCode, int offset);

	private native int PSLGetLicenseStatus(int[] licStat, int offset);

	private native int PSLGetNumbAvailProc(int[] maxProc, int[] availProc, int offset);

	private native int PSLGetFeatureStatus(byte[] featureName, int[] fStat, int offset);

	private native int PSLImportLicense(byte[] licenseNo, byte[] certContainer, int[] licStat, int offset);

	private native int PSLGetUDFValue(byte[] UDFName, byte[] UDFValue, int offset);

	//Use this so we don't have to keep track of the library or
	// its handle.

	public
	PSL(
	NALP nalp,
	int		oset
	)
	{
		this.nalp = nalp;
		this.offset = oset;
	}

	public void
	PSLSetLicNo(
	String  licNo
	)
	{
		licenseNo = licNo;
	}

	public String
	PSLGetLicNo(
	)
	{
		if (licenseNo == null)
		{
			return "";
		}

		return licenseNo;
	}

/**
 * @brief Gets the vewrsion of the PSL library being accessed. See PSLGetVersion()
 *
 * @return A String containing the version of the Nalpeiron Passive library
 *
 * @throws NalpError :   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */
	public String
	callPSLGetVersion()
	throws NalpError
	{
		byte[]	pslVersion = new byte[256];
		int		i;


		i = PSLGetVersion(pslVersion, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		//PSL library uses UTF-8 internally
		try
		{
			return new String(pslVersion, 0, i, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * @brief Retrieves the library stamp options for the current library.
 * See NSLGetLibraryOptions().
 *
 * @return An unsigned 64 bit integer containing the library options.
 * (\ref LIBOPTS)
 *
 * @throws NalpError:   If there was a problem calling the NSLfunction,
 *  this error will be thrown (\ref V10ERROR)
 */
    public long
    callPSLGetLibraryOptions()
    throws NalpError
    {
        long[] libOptions = new long[1];
        int i;


        i = PSLGetLibraryOptions(libOptions, offset);

        if (i < 0)
        {
            throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
        }

        return libOptions[0];
    }

/**
 * @brief Gets the computer ID of the current system.  See PSLGetComputerID().
 *
 * @return  A string containing the computerID of the system.
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */

	public String
	callPSLGetComputerID()
	throws NalpError
	{
		byte[]	pslCompID = new byte[50];
		int		i;


		i = PSLGetComputerID(pslCompID, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		//PSL library uses UTF-8 internally
		try
		{
			return new String(pslCompID, 0, i, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * @brief Get the lease expiration time in seconds.  See PSLGetLeaseExpSec().
 *
 * @return A integer containing the number of seconds remaining in the
 * current lease.  If the lease is expired the number of seconds remaining
 * will be 0 (zero).
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */
	public int
	callPSLGetLeaseExpSec()
	throws NalpError
	{
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int	i;


		i = PSLGetLeaseExpSec(expSec, expEpoch, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		return expSec[0];
	}

/**
 * @brief Get the lease expiration date.  See PSLGetLeaseExpDate().
 *
 * @return  Returns a string containing the expiration of the license as a
 * time/date string (Wed Jul 17 19:59:12 2013, for example).  If the license
 * has expired the date will contain the system's current time.
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */
	public String
	callPSLGetLeaseExpDate()
	throws NalpError
	{
		byte[]	pslExpDate = new byte[128];
		int		i;


		i = PSLGetLeaseExpDate(pslExpDate, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		//PSL library uses UTF-8 internally
		try
		{
			return new String(pslExpDate, 0, i, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}


/**
 * @brief Get remaining seconds in maintanence period.  Available
 * only on certain platforms.
 *
 * @return A integer containing the number of seconds remaining in the
 * current maintanence period.  If the maintanence period has expired the number
 * of seconds remaining will be 0 (zero).
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 * this error will be thrown (\ref V10ERROR)
 *
 */
	public int
	callPSLGetMaintExpSec()
	throws NalpError
	{
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int	i;


		i = PSLGetMaintExpSec(expSec, expEpoch, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		return expSec[0];
	}

/**
 * @brief Get the maintanence expiration date.  Available only on certain platforms.
 *
 * @return  Returns a string containing the maintanence date of the license as a
 * time/date string (Wed Jul 17 19:59:12 2013, for example).  If the license
 * maintancence date has passed the return will contain the system's current time.
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */
	public String
	callPSLGetMaintExpDate()
	throws NalpError
	{
		byte[]	pslExpDate = new byte[128];
		int		i;


		i = PSLGetMaintExpDate(pslExpDate, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		//PSL library uses UTF-8 internally
		try
		{
			return new String(pslExpDate, 0, i, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * @brief Imports a license from the given certificate. See PSLImportCertificate().
 *
 * @param licenseNo the license number associated with the given license
 * certificate
 *
 * @param cert Either the certificate itself passed in as a NULL
 * terminated string or the full path to a file containing the certificate.
 *
 * @return The license status of the imported certificate.
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */
	public int
	callPSLImportLicense(String licenseNo, String cert)
	throws NalpError
	{
		int[] licStat = new int[1];
		int	i;

		try
		{
			i = PSLImportLicense((licenseNo + '\000').getBytes("UTF-8"),
					(cert + '\000').getBytes("UTF-8"), licStat, offset);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		return licStat[0];
	}


/**
 * @brief Verifies that the shared library you are accessing is the
 * library you stamped at Nalpeiron's website. It does this by checking
 * the customerID and productID sent in against the stamped values. See
 * PSLValidateLibrary(). Any negative return value, even if it is not a 
 * known error code, is cause to consider the library invalid.
 *
 * @param custID a unsigned 32 bit integer containing your Nalpeiron
 * customer ID.
 *
 * @param prodID a unsigned 32 bit integer containing your Nalpeiron
 * product ID.
 *
 * @return = 0(zero) is returned if the customerID/productID passed into the
 * function match those from the library. A non-zero values indicates a
 * validation failure.
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */
	public int
	callPSLValidateLibrary(int custID, int prodID)
	throws NalpError
	{
		int	i;


		i = PSLValidateLibrary(custID, prodID, offset);

		if (i < 0)
		{
			log.error("Error loading library: %d" + i);
			//throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		return i;
	}

/**
 * @brief Retrieves the status of the current license.  Negative values
 * indicate an invalid license state.  See PSLGetLicenseStatus().
 *
 *
 * @return An integer representing the status of the current license
 * (see \ref PRODSTAT)
 *
 * @throws NalpError:   If there was a problem calling the PSLfunction,
 *  this error will be thrown (\ref V10ERROR)
 */
	public int callPSLGetLicenseStatus()
	throws NalpError
	{
		int[] licStat = new int[1];
		int	i;


		i = PSLGetLicenseStatus(licStat, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		return licStat[0];
	}

/**
 * @brief If the number of concurrent processes was limited when the
 * system's license was created, this function will return the maximum
 * number of allowed processes.
 *
 * A license may limit the number of simultaneous copies allowed
 * to run on a specific computer.  This limit is disabled by default
 * and, if desired, must be enabled on the Nalpeiron server.  The number
 * of copies available is set to noProcs+1 and availProc is the number
 * of copies still available for use.  When an application instance is
 * started and returns availProc=0, the number of instances exceeds the
 * maximum allowed and this instance should be terminated.
 *
 * When disabled, this fuction will always return zero (0) as noProcs and
 * one (1) as the number of availProc. NOTE limiting the number of
 * processes allowed on a machine ("Number of concurrent Processes"
 * setting on server) is not affected by the "Concurrency Mode" setting.
 * See PSLGetNumbAvailProc()
 *
 * @return Maximum number of processes allowed
 *
 * @throws NalpError:   If there was a problem calling the PSLfunction,
 *  this error will be thrown (\ref V10ERROR)
 */
	public int callPSLGetMaxProcs()
	throws NalpError
	{
		int[] maxProc = new int[1];
		int[] availProc = new int[1];
		int	i;


		i = PSLGetNumbAvailProc(maxProc, availProc, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		return maxProc[0];
	}

/**
 * @brief If the number of concurrent processes was limited when the
 * system's license was created, this function will return the number
 * of allowed processes still remaining for use.
 *
 * A license may limit the number of simultaneous copies allowed
 * to run on a specific computer.  This limit is disabled by default
 * and, if desired, must be enabled on the Nalpeiron server.  The number
 * of copies available is set to noProcs+1 and availProc is the number
 * of copies still available for use.  When an application instance is
 * started and returns availProc=0, the number of instances exceeds the
 * maximum allowed and this instance should be terminated.
 *
 * When disabled, this fuction will always return zero (0) as noProcs and
 * one (1) as the number of availProc. NOTE limiting the number of
 * processes allowed on a machine ("Number of concurrent Processes"
 * setting on server) is not affected by the "Concurrency Mode" setting.
 * See PSLGetNumbAvailProc()
 *
 * @return Number of processes still available for use.
 *
 * @throws NalpError:   If there was a problem calling the PSLfunction,
 *  this error will be thrown (\ref V10ERROR)
 */
	public int callPSLGetAvailProcs()
	throws NalpError
	{
		int[] maxProc = new int[1];
		int[] availProc = new int[1];
		int	i;


		i = PSLGetNumbAvailProc(maxProc, availProc, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		return availProc[0];
	}

/**
 * @brief Retrieves the license code used with the current license.  NOTE: a
 * license code will only be present if one has already been assigned to the
 * current license via PSLGetLicense, PSLImportCert, etc. See PSLGetLicenseCode().
 *
 * @return String containing the current license code.
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */
	public String callPSLGetLicenseCode()
	throws NalpError
	{
		byte[]	pslLicCode = new byte[256];
		int	i;


		i = PSLGetLicenseCode(pslLicCode, offset);

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		//PSL library uses UTF-8 internally
		try
		{
			return new String(pslLicCode, 0, i, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

/**
 * @brief Checks the current license for the status of featureName.
 * See PSLGetFeatureStatus().
 *
 * @param featureName a string containing the five (5) character feature code
 * of the feature to be checked out.
 *
 * @return An integer representing the status of the checked-out
 * feature (\ref FEATSTATUS)
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */
	public int callPSLGetFeatureStatus(String featureName)
	throws NalpError
	{
		int[] featureStatus = new int[1];
		int	i;


		try
		{
			i = PSLGetFeatureStatus((featureName + '\000').getBytes("UTF-8"),
				featureStatus, offset);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		return featureStatus[0];
	}

/**
 * @brief Retrieve the value of a UDF (AA). See PSLGetUDFValue().
 *
 * @param UDFName a string containing the five (5) character UDF code
 * of the value to be accessed
 *
 * @return A string containing the UDF value.
 *
 * @throws NalpError:   If there was a problem calling the PSL function,
 *  this error will be thrown (\ref V10ERROR)
 */
	public String
	callPSLGetUDFValue(String UDFName)
	throws NalpError
	{
		byte[]	UDFValue = new byte[4096];
		int		i;


		try
		{
			i = PSLGetUDFValue((UDFName + '\000').getBytes("UTF-8"),
				UDFValue, offset);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0)
		{
			throw new NalpError(i, nalp.callPSLGetErrorMsg(i));
		}

		//PSL library uses UTF-8 internally
		try
		{
			return new String(UDFValue, 0, i, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}
}

/** @} */ //end of PSLJAVA

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
