//
// nsl.java
//
// Created July 19 2012
// R. D. Ramey
//

package com.nalpeiron.nalplibrary;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Generic and initialization functions for Nalpeiron V10 library
 */
public class NSL {
	private final NALP nalp;

	//Security offset. Set it in constructor so we can adjust
	// returns from NSL functions
	private final int offset;

	private String licenseNo;


	private native int NSLValidateLibrary(long custID, long prodID, int offset);

	private native int NSLGetVersion(byte[] nslVersion, int offset);

	private native int NSLGetComputerID(byte[] computerID, int offset);

	private native int NSLGetHostName(byte[] hostName, int offset);

	private native int NSLGetLeaseExpDate(byte[] expDate, int offset);

	private native int NSLGetLeaseExpSec(int[] expSec, int[] expEpoch, int offset);

	private native int NSLGetRefreshExpDate(byte[] expDate, int offset);

	private native int NSLGetRefreshExpSec(int[] expSec, int[] expEpoch, int offset);

	private native int NSLGetSubExpDate(byte[] expDate, int offset);

	private native int NSLGetSubExpSec(int[] expSec, int[] expEpoch, int offset);

	private native int NSLGetMaintExpDate(byte[] expDate, int offset);

	private native int NSLGetMaintExpSec(int[] expSec, int[] expEpoch, int offset);

	private native int NSLGetTrialExpDate(byte[] expDate, int offset);

	private native int NSLGetTrialExpSec(int[] expSec, int[] expEpoch, int offset);

	private native int NSLGetLicenseCode(byte[] licCode, int offset);

	private native int NSLGetLicenseStatus(int[] licStat, int offset);

	private native int NSLGetLicenseInfo(int[] licenseType,
										 int[] actType, int offset);

	private native int NSLGetLibraryOptions(long[] LibOptions, int offset);

	private native int NSLGetVMInfo(int[] isVM, byte[] vmType, int offset);

	private native int NSLGetNumbAvailProc(int[] maxProc,
										   int[] availProc, int offset);

	private native int NSLGetTimeStamp(int[] timeStamp, int offset);

	private native int NSLGetFeatureStatus(byte[] featureName,
										   int[] fStat, int offset);

	private native int NSLCheckoutFeature(byte[] featureName,
										  byte[] licCode, int[] fStat, int offset);

	private native int NSLReturnFeature(byte[] featureName,
										byte[] licCode, int offset);

	private native int NSLGetPoolStatus(byte[] PoolName,
										int[] pMax, int[] pAmt, int[] pStat, int offset);

	private native int NSLGetPoolInfo(byte[] PoolName,
									  int[] pMax, int[] pAmt, int[] pStat, int offset);

	private native int NSLCheckoutPool(byte[] poolName,
									   byte[] licCode, int amt, int[] pStat, int offset);

	private native int NSLReturnPool(byte[] poolName,
									 byte[] licCode, int amt, int offset);

	private native int NSLCheckoutTokens(byte[] poolName,
										 byte[] licCode, int amt, int[] pStat, int offset);

	private native int NSLConsumeTokens(byte[] poolName,
										byte[] licCode, int amt, int offset);

	private native int NSLReturnTokens(byte[] poolName,
									   byte[] licCode, int amt, int offset);

	private native int NSLGetTokenInfo(byte[] PoolName,
									   int[] pMax, int[] pAmt, int[] pStat, int offset);

	private native int NSLGetLicense(byte[] licenseNo,
									 byte[] xmlRegInfo, int[] licStat, int offset);

	private native int NSLObtainLicense(byte[] licenseNo,
										byte[] xmlRegInfo, byte[] specialID, int[] licStat, int offset);

	private native int NSLReturnLicense(byte[] licenseNo, int[] licStat, int offset);

	private native int NSLImportCertificate(byte[] cert,
											byte[] licenseNo, int[] licStat, int offset);

	private native int NSLGetActivationCertReq(byte[] licenseNo,
											   byte[] xmlRegInfo, byte[] cert, int offset);

	private native int NSLRequestActivationCert(byte[] licenseNo,
												byte[] xmlRegInfo, byte[] specialID, byte[] cert, int offset);

	private native int NSLGetDeactivationCertReq(byte[] licenseNo,
												 byte[] cert, int offset);

	private native int NSLGetUDFValue(byte[] UDFName, byte[] UDFValue, int offset);

	private native int NSLReadSecStore(byte[] rawkey,
									   byte[] storename, byte[] secstorevalue, int offset);

	private native int NSLWriteSecStore(byte[] rawkey,
										byte[] storename, byte[] secstorevalue, int offset);

	private native int NSLRemoteCallV(byte[] rpcName, String[] varNames,
									  String[] varValues, byte[] rpcReturn, int offset);

	private native int NSLRegister(byte[] licenseNo, byte[] xmlRegInfo, int offset);

	private native int NSLTestConnection(int offset);

	private native int NSLTestConnection2(long connTO, long transTO, int offset);

	private native int NSLSetCredentials(byte[] classname, byte[] methname,
										 byte[] username, byte[] password, byte[] data, int offset);

	private native int NSLSetCredentialsSSO(byte[] ssoToken,
											int[] expEpoch, byte[] inData, int offset);

	private native int NSLCheckCredentials(int offset);

	private native int NSLGetCredentials(int[] lastRet,
										 byte[] username, byte[] inData, byte[] outData, int offset);

	private native int NSLClearCredentials(int offset);

	private native int NSLGetNewLicenseCode(byte[] profile,
											byte[] licCode, int offset);

	private native int NSLGetPreset(int messNo, byte[] message, int offset);

	private native ArrayList<String> NSLGetMsgByDate(int[] retVal,
													 byte[] message, int offset);

	//Use this so we don't have to keep track of the library or
	// its handle.
	public NSL(
			NALP nalp,
			int oset
	) {
		this.nalp = nalp;
		this.offset = oset;
	}

	public void NSLSetLicNo(
			String licNo
	) {
		licenseNo = licNo;
	}

	public String NSLGetLicNo(
	) {
		if (licenseNo == null) {
			return "";
		}

		return licenseNo;
	}

	/**
	 * @return A String containing the version of the Nalpeiron library
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Gets the vewrsion of the NSL library being accessed. See NSLGetVersion()
	 */
	public String callNSLGetVersion()
			throws NalpError {
		byte[] nslVersion = new byte[256];
		int i;


		i = NSLGetVersion(nslVersion, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslVersion, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Gets the computer ID of the current system.  See NSLGetComputerID().
	 * @return A string containing the computerID of the system.
	 */
	public String callNSLGetComputerID()
			throws NalpError {
		byte[] nslCompID = new byte[50];
		int i;


		i = NSLGetComputerID(nslCompID, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		//String(bytes, offset, length, charset)
		return new String(nslCompID, 0, i, StandardCharsets.UTF_8);
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the name or IP address of the Nalpeiron server or daemon used
	 * for licensing.  See NSLGetHostName().
	 * @return A string containing the hostname or IP address of the Nalpeiron
	 * server used for licensing.
	 */
	public String
	callNSLGetHostName()
			throws NalpError {
		byte[] nslHostName = new byte[128];
		int i;


		i = NSLGetHostName(nslHostName, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslHostName, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @return A integer containing the number of seconds remaining in the
	 * current lease.  If the lease is expired the number of seconds remaining
	 * will be 0 (zero).
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the lease expiration time in seconds.  See NSLGetLeaseExpSec().
	 */
	public int
	callNSLGetLeaseExpSec()
			throws NalpError {
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int i;


		i = NSLGetLeaseExpSec(expSec, expEpoch, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return expSec[0];
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the lease expiration date.  See NSLGetLeaseExpDate().
	 * @return Returns a string containing the expiration of the license as a
	 * time/date string (Wed Jul 17 19:59:12 2013, for example).  If the license
	 * has expired the date will contain the system's current time.
	 */
	public String
	callNSLGetLeaseExpDate()
			throws NalpError {
		byte[] nslExpDate = new byte[128];
		int i;


		i = NSLGetLeaseExpDate(nslExpDate, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslExpDate, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @return A integer containing the number of seconds remaining in the
	 * current refresh period.  If the refresh period has expired the number
	 * of seconds remaining will be 0 (zero).
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get remaining seconds until refresh.  Available only on certain platforms.
	 */
	public int
	callNSLGetRefreshExpSec()
			throws NalpError {
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int i;


		i = NSLGetRefreshExpSec(expSec, expEpoch, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return expSec[0];
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the refresh date.  Available only on certain platforms.
	 * @return Returns a string containing the refresh date of the license as a
	 * time/date string (Wed Jul 17 19:59:12 2013, for example).  If the license
	 * refresh date has passed the return will contain the system's current time.
	 */
	public String
	callNSLGetRefreshExpDate()
			throws NalpError {
		byte[] nslExpDate = new byte[128];
		int i;


		i = NSLGetRefreshExpDate(nslExpDate, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslExpDate, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}


	/**
	 * @return A integer containing the number of seconds remaining in the
	 * current subscription period.  If the subscription period has expired the number
	 * of seconds remaining will be 0 (zero).
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the seconds remaining in the subscription period.  See NSLGetSubExpSec().
	 */
	public int
	callNSLGetSubExpSec()
			throws NalpError {
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int i;


		i = NSLGetSubExpSec(expSec, expEpoch, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return expSec[0];
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the end date of the subscription period.  See NSLGetSubExpDate().
	 * @return Returns a string containing the end subscription date of the license as a
	 * time/date string (Wed Jul 17 19:59:12 2013, for example).  If the license's
	 * subscription date has passed the return will contain the system's current time.
	 */
	public String
	callNSLGetSubExpDate()
			throws NalpError {
		byte[] nslExpDate = new byte[128];
		int i;


		i = NSLGetSubExpDate(nslExpDate, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslExpDate, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}


	/**
	 * @return A integer containing the number of seconds remaining in the
	 * current maintenance period.  If the maintenance period has expired the number
	 * of seconds remaining will be 0 (zero).
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the number of seconds remaining in the maintanence period.
	 * See NSLGetMaintExpSec().
	 */
	public int
	callNSLGetMaintExpSec()
			throws NalpError {
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int i;


		i = NSLGetMaintExpSec(expSec, expEpoch, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return expSec[0];
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the expiration date of the maintanence period. See NSLGetMaintExpDate().
	 * @return Returns a string containing the end maintanence date of the license as a
	 * time/date string (Wed Jul 17 19:59:12 2013, for example).  If the license's
	 * maintanence date has passed the return will contain the system's current time.
	 */
	public String
	callNSLGetMaintExpDate()
			throws NalpError {
		byte[] nslExpDate = new byte[128];
		int i;


		i = NSLGetMaintExpDate(nslExpDate, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslExpDate, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}


	/**
	 * @return A integer containing the number of seconds remaining in the
	 * current trial period.  If the trial period has expired the number
	 * of seconds remaining will be 0 (zero).
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the number of seconds remaining in the trial period.
	 * See NSLGetTrialExpSec().
	 */
	public int
	callNSLGetTrialExpSec()
			throws NalpError {
		int[] expSec = new int[1];
		int[] expEpoch = new int[1];
		int i;


		i = NSLGetTrialExpSec(expSec, expEpoch, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return expSec[0];
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the date of the trial expiration.  See NSLGetTrialExpDate().
	 * @return Returns a string containing the end trial date of the license as a
	 * time/date string (Wed Jul 17 19:59:12 2013, for example).  If the license's
	 * trial date has passed the return will contain the system's current time.
	 */
	public String
	callNSLGetTrialExpDate()
			throws NalpError {
		byte[] nslExpDate = new byte[128];
		int i;


		i = NSLGetTrialExpDate(nslExpDate, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslExpDate, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @param licenseNo  If licenseNo is non-NULL that number is used in the
	 *                   attempt to retrieve a license. If the licenseNo is NULL, the function
	 *                   attempts to retrieve a trial license.
	 * @param xmlRegInfo Optional registration information may be passed to
	 *                   the Nalpeiron server using the xmlRegInfo string.  If there is no
	 *                   registration information, this pointer should be NULL.
	 * @return The status of the retrieved license (\ref PRODSTAT)
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Contacts the SOAP server for a new license or a license refresh. See
	 * NSLGetLicense()
	 * @deprecated June 2020 This function is deprecated and will be removed from a
	 * future version of the NSL library.  Use callNSLObtainLicense
	 * (\ref callNSLObtainLicense) instead.
	 */
	public int
	callNSLGetLicense(String licenseNo, String xmlRegInfo)
			throws NalpError {
		int[] licStat = new int[1];
		int i;


		try {
			i = NSLGetLicense((licenseNo + '\000').getBytes("UTF-8"),
					(xmlRegInfo + '\000').getBytes("UTF-8"),
					licStat, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return licStat[0];
	}

	/**
	 * @param licenseNo  If licenseNo is non-NULL that number is used in the
	 *                   attempt to retrieve a license. If the licenseNo is NULL, the function
	 *                   attempts to retrieve a trial license.
	 * @param xmlRegInfo Optional registration information may be passed to
	 *                   the Nalpeiron server using the xmlRegInfo string.  If there is no
	 *                   registration information, this pointer should be NULL.
	 * @param specialID  optional system identifier.  If specified, Nalpeiron
	 *                   server will include this value inside an application agility field called
	 *                   $SpecialID which can be accessed by the calling program via
	 *                   NSLGetUDFValue (\ref callNSLGetUDFValue).  If this value has been specified
	 *                   before (ie in a previous call to callNSLObtainLicense, for instance) the same
	 *                   value must be specified when requesting the license in the future else the request
	 *                   will be rejected by the Nalpeiron server.  Once set, the value may only be
	 *                   cleared from the license code via manual intervention on the Nalpeiron server.
	 * @return The status of the retrieved license (\ref PRODSTAT)
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Contacts the SOAP server for a new license or a license refresh. See
	 * NSLObtainLicense()
	 */
	public int
	callNSLObtainLicense(String licenseNo, String xmlRegInfo, String specialID)
			throws NalpError {
		int[] licStat = new int[1];
		int i;


		try {
			i = NSLObtainLicense((licenseNo + '\000').getBytes("UTF-8"),
					(xmlRegInfo + '\000').getBytes("UTF-8"),
					(specialID + '\000').getBytes("UTF-8"),
					licStat, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return licStat[0];
	}

	/**
	 * @param licenseNo The license code associated with the current license
	 * @return The status of the license after the return attempt.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Returns the current license to the Nalpeiron server.
	 * See NSLReturnLicense().
	 */
	public int
	callNSLReturnLicense(String licenseNo)
			throws NalpError {
		int[] licStat = new int[1];
		int i;


		try {
			i = NSLReturnLicense((licenseNo + '\000').getBytes("UTF-8"), licStat, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return licStat[0];
	}

	/**
	 * @param licenseNo the license number associated with the given license
	 *                  certificate
	 * @param cert      Either the certificate itself passed in as a NULL
	 *                  terminated string or the full path to a file containing the certificate.
	 * @return The license status of the imported certificate.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Imports a license from the given certificate. See NSLImportCertificate().
	 */
	public int
	callNSLImportCertificate(String licenseNo, String cert)
			throws NalpError {
		int[] licStat = new int[1];
		int i;

		try {
			i = NSLImportCertificate((cert + '\000').getBytes("UTF-8"),
					(licenseNo + '\000').getBytes("UTF-8"), licStat, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Check for offlineState returns
		if (cert.equals("")) {
			licStat[0] = i;
		}

		return licStat[0];
	}


	/**
	 * @param licenseNo  the license number to be used in creating the certificate
	 *                   request.  If the licenseNo is NULL, an attempt for a trial license certificate
	 *                   will be made.
	 * @param xmlRegInfo Optional registration information may be passed to
	 *                   the Nalpeiron server using the xmlRegInfo string.  If there is no
	 *                   registration information, this pointer should be NULL.
	 * @return A string containing the activation certificate request.  NOTE the
	 * byte array used to hold the cert is allocated to 8192 bytes.  This is
	 * large enough for most activation requests.  However, you will need to
	 * monitor the size of your of your certificate and increase this size as
	 * needed.  If you receive a -9015 "Java or JNI memory error" return from this
	 * function the most likely cause is a nslCert array that is too small.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Generate an request for a activation certificate from the
	 * Nalpeiron server (ie offline activation).  The resulting request can
	 * be emailed or otherwise sent to Nalpeiron. See NSLGetActivationCertReq().
	 * @deprecated June 2020 This function is deprecated and will be removed from a
	 * future version of the NSL library.  Use callNSLRequestActivationCert
	 * (\ref callNSLRequestActivationCert) instead.
	 */
	public String
	callNSLGetActivationCertReq(String licenseNo, String xmlRegInfo)
			throws NalpError {
		byte[] nslCert = new byte[8192];
		int i;


		try {
			i = NSLGetActivationCertReq((licenseNo + '\000').getBytes("UTF-8"),
					(xmlRegInfo + '\000').getBytes("UTF-8"), nslCert, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslCert, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}


	/**
	 * @param licenseNo  the license number to be used in creating the certificate
	 *                   request.  If the licenseNo is NULL, an attempt for a trial license certificate
	 *                   will be made.
	 * @param xmlRegInfo Optional registration information may be passed to
	 *                   the Nalpeiron server using the xmlRegInfo string.  If there is no
	 *                   registration information, this pointer should be NULL.
	 * @param specialID  Optional user supplied string.  If present in an
	 *                   activation certificate request the Nalpeiron server will place the
	 *                   supplied value in a special application agility field call $SpecialID. This
	 *                   field can be queried using callNSLGetUDFValue (\ref callNSLGetUDFValue) to
	 *                   access the value from the calling program.
	 * @return A string containing the activation certificate request.  NOTE the
	 * byte array used to hold the cert is allocated to 8192 bytes.  This is
	 * large enough for most activation requests.  However, you will need to
	 * monitor the size of your of your certificate and increase this size as
	 * needed.  If you receive a -9015 "Java or JNI memory error" return from this
	 * function the most likely cause is a nslCert array that is too small.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Generate an request for a activation certificate from the
	 * Nalpeiron server (ie offline activation).  The resulting request can
	 * be emailed or otherwise sent to Nalpeiron. See NSLRequestActivationCert
	 * (/ref NSLRequestActivationCert).
	 */
	public String
	callNSLRequestActivationCert(String licenseNo,
								 String xmlRegInfo, String specialID)
			throws NalpError {
		byte[] nslCert = new byte[8192];
		int i;


		try {
			i = NSLRequestActivationCert((licenseNo + '\000').getBytes("UTF-8"),
					(xmlRegInfo + '\000').getBytes("UTF-8"),
					(specialID + '\000').getBytes("UTF-8"), nslCert, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslCert, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}


	/**
	 * @param licenseNo the license number used to obtain the license.
	 * @return A string containing the deactivation certificate request.  NOTE the
	 * byte array used to hold the cert is allocated to 8192 bytes.  This is
	 * large enough for most deactivation requests.  However, you will need to
	 * monitor the size of your of your certificate and increase this size as
	 * needed.  If you receive a -9015 "Java or JNI memory error" return from this
	 * function the most likely cause is a nslCert array that is too small.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Generate a request for a deactivation certificate from the
	 * Nalpeiron server (ie offline deactivation).  The resulting request can
	 * be emailed or otherwise sent to Nalpeiron.  See NSLGetDeactivationCertReq().
	 */
	public String
	callNSLGetDeactivationCertReq(String licenseNo)
			throws NalpError {
		byte[] nslCert = new byte[8192];
		int i;


		try {
			i = NSLGetDeactivationCertReq((licenseNo + '\000').getBytes("UTF-8"), nslCert, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslCert, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}


	/**
	 * @param custID a unsigned 32 bit integer containing your Nalpeiron
	 *               customer ID.
	 * @param prodID a unsigned 32 bit integer containing your Nalpeiron
	 *               product ID.
	 * @return = 0(zero) is returned if the customerID/productID passed into the
	 * function match those from the library. A non-zero values indicates a
	 * validation failure.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Verifies that the shared library you are accessing is the
	 * library you stamped at Nalpeiron's website. It does this by checking
	 * the customerID and productID sent in against the stamped values. See
	 * NSLValidateLibrary(). Any negative return value, even if it is not a
	 * known error code, is cause to consider the library invalid.
	 */
	public int
	callNSLValidateLibrary(int custID, int prodID)
			throws NalpError {
		int i;


		i = NSLValidateLibrary(custID, prodID, offset);

		if (i < 0) {
			System.out.println("Error loading library: " + i);
			//throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * @return An integer representing the status of the current license
	 * (see \ref PRODSTAT)
	 * @throws NalpError: If there was a problem calling the NSLfunction,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Retrieves the status of the current license.  Negative values
	 * indicate an invalid license state.  See NSLGetLicenseStatus().
	 */
	public int
	callNSLGetLicenseStatus()
			throws NalpError {
		int[] licStat = new int[1];
		int i;


		i = NSLGetLicenseStatus(licStat, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return licStat[0];
	}

	/**
	 * @return An unsigned 32 bit integer representing the license type.
	 * (\ref LICTYPE)
	 * @throws NalpError: If there was a problem calling the NSLfunction,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Retrieves the license type for the current license.
	 * See NSLGetLicenseInfo().
	 */
	public int
	callNSLGetLicenseType()
			throws NalpError {
		int[] licType = new int[1];
		int[] actType = new int[1];
		int i;


		i = NSLGetLicenseInfo(licType, actType, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return licType[0];
	}

	/**
	 * @return An unsigned 64 bit integer containing the library options.
	 * (\ref LIBOPTS)
	 * @throws NalpError: If there was a problem calling the NSLfunction,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Retrieves the library stamp options for the current library.
	 * See NSLGetLibraryOptions().
	 */
	public long
	callNSLGetLibraryOptions()
			throws NalpError {
		long[] libOptions = new long[1];
		int i;


		i = NSLGetLibraryOptions(libOptions, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return libOptions[0];
	}

	/**
	 * @return A string descibing the type of machine: physical or virtual.
	 * If virtual the string attempts to describe the type of virtual machine.
	 * (\ref LICTYPE)
	 * @throws NalpError: If there was a problem calling the NSLfunction,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Retrieves virtual machine information for the system.
	 * See NSLGetVMInfo().
	 */
	public String
	callNSLGetVMInfo()
			throws NalpError {
		int[] isVM = new int[1];
		byte[] vmType = new byte[128];
		int i;


		i = NSLGetVMInfo(isVM, vmType, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(vmType, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @return An unsigned 32 bit integer representing the activation type.
	 * (\ref LICTYPE)
	 * @throws NalpError: If there was a problem calling the NSLfunction,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Retrieves the activation type for the current license.
	 * See NSLGetLicenseInfo().
	 */
	public int callNSLGetActivationType()
			throws NalpError {
		int[] licType = new int[1];
		int[] actType = new int[1];
		int i;


		i = NSLGetLicenseInfo(licType, actType, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return actType[0];
	}

	/**
	 * @return Maximum number of processes allowed
	 * @throws NalpError: If there was a problem calling the NSLfunction,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief If the number of concurrent processes was limited when the
	 * system's license was created, this function will return the maximum
	 * number of allowed processes.
	 * <p>
	 * A license may limit the number of simultaneous copies allowed
	 * to run on a specific computer.  This limit is disabled by default
	 * and, if desired, must be enabled on the Nalpeiron server.  The number
	 * of copies available is set to noProcs+1 and availProc is the number
	 * of copies still available for use.  When an application instance is
	 * started and returns availProc=0, the number of instances exceeds the
	 * maximum allowed and this instance should be terminated.
	 * <p>
	 * When disabled, this fuction will always return zero (0) as noProcs and
	 * one (1) as the number of availProc. NOTE limiting the number of
	 * processes allowed on a machine ("Number of concurrent Processes"
	 * setting on server) is not affected by the "Concurrency Mode" setting.
	 * See NSLGetNumbAvailProc()
	 */
	public int callNSLGetMaxProcs()
			throws NalpError {
		int[] maxProc = new int[1];
		int[] availProc = new int[1];
		int i;


		i = NSLGetNumbAvailProc(maxProc, availProc, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return maxProc[0];
	}

	/**
	 * @return Number of processes still available for use.
	 * @throws NalpError: If there was a problem calling the NSLfunction,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief If the number of concurrent processes was limited when the
	 * system's license was created, this function will return the number
	 * of allowed processes still remaining for use.
	 * <p>
	 * A license may limit the number of simultaneous copies allowed
	 * to run on a specific computer.  This limit is disabled by default
	 * and, if desired, must be enabled on the Nalpeiron server.  The number
	 * of copies available is set to noProcs+1 and availProc is the number
	 * of copies still available for use.  When an application instance is
	 * started and returns availProc=0, the number of instances exceeds the
	 * maximum allowed and this instance should be terminated.
	 * <p>
	 * When disabled, this fuction will always return zero (0) as noProcs and
	 * one (1) as the number of availProc. NOTE limiting the number of
	 * processes allowed on a machine ("Number of concurrent Processes"
	 * setting on server) is not affected by the "Concurrency Mode" setting.
	 * See NSLGetNumbAvailProc()
	 */
	public int callNSLGetAvailProcs()
			throws NalpError {
		int[] maxProc = new int[1];
		int[] availProc = new int[1];
		int i;


		i = NSLGetNumbAvailProc(maxProc, availProc, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return availProc[0];
	}

	/**
	 * @return String containing the current license code.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Retrieves the license code used with the current license.  NOTE: a
	 * license code will only be present if one has already been assigned to the
	 * current license via NSLGetLicense, NSLImportCert, etc. See NSLGetLicenseCode().
	 */
	public String callNSLGetLicenseCode()
			throws NalpError {
		byte[] nslLicCode = new byte[256];
		int i;


		i = NSLGetLicenseCode(nslLicCode, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslLicCode, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @return An integer containing the current time in seconds since epoch start.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Retrieves the current time (seconds since epoch start) from
	 * the Nalpeiron daemon or server. See NSLGetTimeStamp().
	 */
	public int
	callNSLGetTimeStamp()
			throws NalpError {
		int[] timeStamp = new int[1];
		int i;


		i = NSLGetTimeStamp(timeStamp, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return timeStamp[0];
	}

	/**
	 * @param featureName a string containing the five (5) character feature code
	 *                    of the feature to be checked out.
	 * @return An integer representing the status of the checked-out
	 * feature (\ref FEATSTATUS)
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Checks the current license for the status of featureName.
	 * See NSLGetFeatureStatus().
	 */
	public int callNSLGetFeatureStatus(String featureName)
			throws NalpError {
		int[] featureStatus = new int[1];
		int i;


		try {
			i = NSLGetFeatureStatus((featureName + '\000').getBytes("UTF-8"),
					featureStatus, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return featureStatus[0];
	}

	/**
	 * @param featureName a string containing the five (5) character feature code
	 *                    of the feature to be checked out.
	 * @param licenseNo   the license number of the current, valid system license
	 * @return An integer representing the status of the checked-out
	 * feature (\ref FEATSTATUS)
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Checkout one use of a "floating feature" from the Nalpeiron
	 * server or daemon. On return featureStatus will contain the feature
	 * status code.  In addition to checking the return value of the function,
	 * the feature status code must also be checked to ensure a feature
	 * seat was available.  See NSLCheckoutFeature().
	 */
	public int callNSLCheckoutFeature(String featureName, String licenseNo)
			throws NalpError {
		int[] featureStatus = new int[1];
		int i;


		try {
			i = NSLCheckoutFeature((featureName + '\000').getBytes("UTF-8"),
					(licenseNo + '\000').getBytes("UTF-8"),
					featureStatus, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return featureStatus[0];
	}

	/**
	 * @param featureName a string containing the five (5) character feature code
	 *                    of the feature to be checked out.
	 * @param licenseNo   the license number of the current, valid system license
	 * @return 0 on success
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Return one use of a "floating feature" to the Nalpeiron
	 * server or daemon.  See NSLReturnFeature().
	 */
	public int callNSLReturnFeature(String featureName, String licenseNo)
			throws NalpError {
		int i;


		try {
			i = NSLReturnFeature((featureName + '\000').getBytes("UTF-8"),
					(licenseNo + '\000').getBytes("UTF-8"), offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return 0;
	}

	/**
	 * @param poolName a string containing the five (5) character pool code
	 *                 of the pool to be withdrawn from.
	 * @return > 0 The number of elements from the pool currently held by the system.
	 * @return < 0 The status of the pool.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the number of pool elements checked out from the named pool.
	 * See NSLGetPoolStatus().
	 * @deprecated June 2017 This function is deprecated and will be removed from a
	 * future version of the NSL library.  Use callNSLGetPoolInfo
	 * (\ref callNSLGetPoolInfo) instead.
	 */
	public int callNSLGetPoolStatus(String poolName)
			throws NalpError {
		int[] poolStatus = new int[1];
		int[] poolAmt = new int[1];
		int[] poolMax = new int[1];
		int i;


		try {
			i = NSLGetPoolStatus((poolName + '\000').getBytes("UTF-8"),
					poolMax, poolAmt, poolStatus, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		if (poolStatus[0] < 0) {
			return poolStatus[0];
		}

		return poolAmt[0];
	}

	/**
	 * @param poolName a string containing the five (5) character pool code
	 *                 of the pool to be withdrawn from.
	 * @return > 0 The number of elements from the pool currently held by the system.
	 * @return < 0 The status of the pool.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the number of pool elements checked out from the named pool.
	 * See NSLGetPoolInfo().
	 */
	public int callNSLGetPoolInfo(String poolName)
			throws NalpError {
		int[] poolStatus = new int[1];
		int[] poolAmt = new int[1];
		int[] poolMax = new int[1];
		int i;


		try {
			i = NSLGetPoolInfo((poolName + '\000').getBytes("UTF-8"),
					poolMax, poolAmt, poolStatus, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		if (poolStatus[0] < 0) {
			return poolStatus[0];
		}

		return poolAmt[0];
	}

	/**
	 * @param poolName a string containing the five (5) character pool code
	 *                 of the pool to be withdrawn from.
	 * @return > 0 The maximum number of elements present in the pool.
	 * @return < 0 The status of the pool.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get the number of pool elements in the named pool.
	 * See NSLGetPoolMax().
	 */
	public int callNSLGetPoolMax(String poolName)
			throws NalpError {
		int[] poolStatus = new int[1];
		int[] poolAmt = new int[1];
		int[] poolMax = new int[1];
		int i;


		try {
			i = NSLGetPoolInfo((poolName + '\000').getBytes("UTF-8"),
					poolMax, poolAmt, poolStatus, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		if (poolStatus[0] < 0) {
			return poolStatus[0];
		}

		return poolMax[0];
	}

	/**
	 * @param poolName  a string containing the five (5) character pool code
	 *                  of the pool to be withdrawn from.
	 * @param licenseNo the license number of the current, valid system license
	 * @param amt       the number of elements to withdraw from the pool
	 * @return The status of the pool (\ref FEATSTATUS)
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Withdraws a specified number of elements from an
	 * element pool. See NSLCheckoutPool().
	 */
	public int callNSLCheckoutPool(String poolName, String licenseNo, int amt)
			throws NalpError {
		int[] poolStatus = new int[1];
		int i;


		try {
			//the library will vet the amt.
			i = NSLCheckoutPool((poolName + '\000').getBytes("UTF-8"),
					(licenseNo + '\000').getBytes("UTF-8"),
					amt, poolStatus, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return poolStatus[0];
	}

	/**
	 * /**
	 *
	 * @param poolName  a string containing the five (5) character pool code
	 *                  of the pool to be withdrawn from.
	 * @param licenseNo the license number of the current, valid system license
	 * @param amt       the number of elements to withdraw from the pool
	 * @return = 0 If the call succeeded
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Returns a specified number of elements to an
	 * element pool. See NSLReturnPool().
	 */
	public int callNSLReturnPool(String poolName, String licenseNo, int amt)
			throws NalpError {
		int i;


		try {
			//the library will vet the amt.
			i = NSLReturnPool((poolName + '\000').getBytes("UTF-8"),
					(licenseNo + '\000').getBytes("UTF-8"),
					amt, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return 0;
	}

	/**
	 * @param tokenName a string containing the five (5) character code
	 *                  of the token pool to be withdrawn from.
	 * @return > 0 The number of elements from the tokens currently held by the system.
	 * @return < 0 The status of the tokesn.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Get information about tokens checked out from the named pool.
	 * See NSLGetTokenInfo().
	 */
	public int callNSLGetTokenInfo(String tokenName)
			throws NalpError {
		int[] tokenStatus = new int[1];
		int[] tokenAmt = new int[1];
		int[] tokenMax = new int[1];
		int i;


		try {
			i = NSLGetTokenInfo((tokenName + '\000').getBytes("UTF-8"),
					tokenMax, tokenAmt, tokenStatus, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		if (tokenStatus[0] < 0) {
			return tokenStatus[0];
		}

		return tokenAmt[0];
	}

	/**
	 * @param tokenName a string containing the five (5) character code
	 *                  of the token pool to be withdrawn from.
	 * @param licenseNo the license number of the current, valid system license
	 * @param amt       the number of elements to withdraw from the pool
	 * @return The status of the token (\ref FEATSTATUS)
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Withdraws a specified number of tokens from an
	 * token pool. See NSLCheckoutTokens().
	 */
	public int callNSLCheckoutTokens(String tokenName, String licenseNo, int amt)
			throws NalpError {
		int[] tokenStatus = new int[1];
		int i;


		try {
			//the library will vet the amt.
			i = NSLCheckoutTokens((tokenName + '\000').getBytes("UTF-8"),
					(licenseNo + '\000').getBytes("UTF-8"),
					amt, tokenStatus, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		if (tokenStatus[0] < 0) {
			return tokenStatus[0];
		}

		return tokenStatus[0];
	}

	/**
	 * @param tokenName a string containing the five (5) character code
	 *                  of the token pool to be consumed from.
	 * @param licenseNo the license number of the current, valid system license
	 * @param amt       the number of elements to consume.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Consumes the specified number of tokens from an tokens
	 * checkedout by NSLCheckoutTokens onto local machine.
	 */
	public int callNSLConsumeTokens(String tokenName, String licenseNo, int amt)
			throws NalpError {
		int[] tokenStatus = new int[1];
		int i;


		try {
			//the library will vet the amt.
			i = NSLConsumeTokens((tokenName + '\000').getBytes("UTF-8"),
					(licenseNo + '\000').getBytes("UTF-8"), amt, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * @param tokenName a string containing the five (5) character code
	 *                  of the token pool to be consumed from.
	 * @param licenseNo the license number of the current, valid system license
	 * @param amt       the number of elements to return.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Returns the specified number of tokens from the local machine
	 * to the Nalpeiron server.
	 */
	public int callNSLReturnTokens(String tokenName, String licenseNo, int amt)
			throws NalpError {
		int[] tokenStatus = new int[1];
		int i;


		try {
			//the library will vet the amt.
			i = NSLReturnTokens((tokenName + '\000').getBytes("UTF-8"),
					(licenseNo + '\000').getBytes("UTF-8"), amt, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * @param UDFName a string containing the five (5) character UDF code
	 *                of the value to be accessed
	 * @return A string containing the UDF value.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Retrieve the value of a UDF (AA). See callNSLGetUDFValue().
	 */
	public String
	callNSLGetUDFValue(String UDFName)
			throws NalpError {
		byte[] UDFValue = new byte[4096];
		int i;


		try {
			i = NSLGetUDFValue((UDFName + '\000').getBytes("UTF-8"),
					UDFValue, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(UDFValue, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @param storename a string containing the five (5) character secure
	 *                  store code of the value to be accessed
	 * @return A string containing the Secure Store value.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Retrieve the value of a Secure Store. See callNSLReadSecStore().
	 */
	public String
	callNSLReadSecStore(String rawkey, String storename)
			throws NalpError {
		byte[] nslStoreVal = new byte[4096];
		int i;

		try {
			i = NSLReadSecStore((rawkey + '\000').getBytes("UTF-8"),
					(storename + '\000').getBytes("UTF-8"), nslStoreVal, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			//String(bytes, offset, length, charset)
			return new String(nslStoreVal, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @param storename a string containing the five (5) character secure
	 *                  store code of the value to be accessed
	 * @return = 0(zero) is returned if the Secure Store write succeeded.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Write the value to a Secure Store. See callNSLWriteSecStore().
	 */
	public int
	callNSLWriteSecStore(String rawkey, String storename, String secstorevalue)
			throws NalpError {
		int i;

		try {
			i = NSLWriteSecStore((rawkey + '\000').getBytes("UTF-8"),
					(storename + '\000').getBytes("UTF-8"),
					(secstorevalue + '\000').getBytes("UTF-8"), offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * @param rpcName   The name of the remote proceedure
	 * @param varNames  an array of strings containing the names of the RPC's
	 *                  parameters.  The order of the parameter names must match the order
	 *                  of the parameter values. That is, name1, name2, name3, must be in the
	 *                  same order as value1, value2, value3
	 * @param varValues an array of strings containing the values to be used
	 *                  for the RPC's parameters.  The order of the parameter names must match
	 *                  the order of the parameter values. That is, name1, name2, name3, must
	 *                  be in the same order as value1, value2, value3
	 * @return A string containing the rpcReturn value, if any.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief Call a remote proceedure on the Nalpeiron server
	 */
	public String
	callNSLRemoteCallV(String rpcName, String[] varNames, String[] varValues)
			throws NalpError {
		byte[] rpcReturn = new byte[4096];
		int i;


		try {
			//Unlike other JNI functions the conversion to UTF-8 is 
			// handled in the JNI section of this call.  Note i < 0
			// on error otherwise contains strlen
			i = NSLRemoteCallV((rpcName + '\000').getBytes("UTF-8"),
					varNames, varValues, rpcReturn, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			return new String(rpcReturn, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief: Call registration function.  Available only on certain platforms.
	 * @return 0 on success, negative value on error
	 */
	public int callNSLRegister(String licenseNo, String xmlRegInfo)
			throws NalpError {
		int i;


		try {
			i = NSLRegister((licenseNo + '\000').getBytes("UTF-8"),
					(xmlRegInfo + '\000').getBytes("UTF-8"), offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief: Tests the network connection to Nalpeiron. See NSLTestConnection().
	 * @return 0 on success
	 * @deprecated June 2018 This function is deprecated and will be removed from a
	 * future version of the NSL library.  Use callNSLTestConnection2 instead
	 * (\ref callNSLTestConnection2) instead.
	 */
	public int callNSLTestConnection()
			throws NalpError {
		int i;


		i = NSLTestConnection(offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}


	/**
	 * @param connTO  if non-zero the number of milliseconds the library should
	 *                wait for the initial connection to the Nalpeiron server to complete before
	 *                timing out.  If 0, the values passed into NalpLibOpen are used.  This value
	 *                overrides any connection timeout specified at library initialization.
	 * @param transTO if non-zero the number of milliseconds the library should
	 *                wait for the entire transaction (this includes the connection time above,
	 *                name look-up time, transfer time, etc) to the Nalpeiron server to complete
	 *                before timing out.  If 0, the passed into NalpLibOpen are used. This value
	 *                overrides any transaction timeout specified at library initialization.
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief: Tests the network connection to Nalpeiron. See NSLTestConnection2().
	 * @return 0 on success
	 */
	public int callNSLTestConnection2(long connTO, long transTO)
			throws NalpError {
		int i;


		if ((connTO < 0) || (transTO < 0)) {
			throw new NalpError(-9006, "Value out of range");
		}

		i = NSLTestConnection2(connTO, transTO, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * @param classname if empty then autentication will take place at
	 *                  Nalpeiron. If non-empty then the user supplied callback with found in
	 *                  classname with methname will be used for authentication.
	 * @param methname  if NULL then autentication will take place at
	 *                  Nalpeiron. If non-NULL then the pointer will be used as a user supplied
	 *                  callback with the form:
	 *                  <p>
	 *                  long function(String username, String password, String inData,
	 *                  ByteBuffer authToken, int[] outDataSize, ByteBuffer outData);
	 *                  <p>
	 *                  This function will be called to authenticate the username/password
	 *                  combination. The function will have the username, password, and data
	 *                  parameters passed to it exactly as the were passed to the NSLSetCredentials
	 *                  function.  This means, for instance, that you could encrypt the password
	 *                  before passing it to NSLSetCredentials for security purposes (of course,
	 *                  that would mean, the userAuthenticator function would need to be able to
	 *                  use an encrypted password). The callback is called immediately (ie in
	 *                  NSLSetCredentials) and again whenever a call to NSLGetLicense is made.
	 * @param username  either a NULL terminated, UTF-8 encoded string containing
	 *                  a valid username for the Nalpeiron server OR a UTF-8 encoded string that
	 *                  will be passed to the userAuthenticator function.  NOTE in the case that
	 *                  a userAuthenticator function is to be used neither the username nor
	 *                  the password are stored in the license file.  This means that the
	 *                  NSLSetCredentials function should be called after any library initialization.
	 *                  The username size is limited to 4k (4096 bytes).
	 * @param password  a NULL terminated, UTF-8 encoded string containing
	 *                  a valid password for the Nalpeiron server OR a UTF-8 encoded string that
	 *                  will be passed to the userAuthenticator function. In the case of the
	 *                  password to be used at Nalpeiron for authentication, the password is never
	 *                  recorded nor transmitted in plain text.  If the password is to be passed
	 *                  to an authentication function (ie userAuthenticator != NULL), then the
	 *                  password is NOT stored in the license file.  This means that the
	 *                  NSLSetCredentials function should be called after any library initialization.
	 *                  The password size is limited to 4k (4096 bytes).
	 * @param inData    an optional bit of data that, if non-NULL, will be passed
	 *                  into the userAuthenticator function.  A copy of this data is made and
	 *                  stored in memory by the library.  Data is NOT stored on disk at any time
	 *                  which means it is lost when the library is closed.  NOTE data is neither
	 *                  used nor stored if a callback is not specified. The maximum allowable size
	 *                  is 4k (4096 bytes).
	 * @return = 0 If the call succeeded
	 * @return < 0 A negative error value is returned (\ref V10ERROR)
	 * @brief Set username and password for future logins to the Nalpeiron
	 * licensing server or for a user provided authentication callback function.
	 * <p>
	 * The Nalpeiron library provides three forms of authentication.  In the
	 * simplest form, the username and password are presented to NSLSetCredentials.
	 * Both are passed to Nalpeiron (the password is never stored or transmitted
	 * as plain text) for validation.  If the Nalpeiron server validates them
	 * a license can be obtains via calls such as NSLGetLicense.
	 * <p>
	 * In a second method, in addition to the username a callback function is
	 * passed into NSLCredentials.  When authentication is required (ie when
	 * NSLSetCredentials is called but also when functions such as NSLGetLicense
	 * and NSLGetActivationCertReq are called) the callback will be called from
	 * the library.  The callback must have the following function prototype:
	 * <p>
	 * int64_t (*userAuth)(char *uname, char *pword, void *inData,
	 * char **authToken, uint32_t *outDataSize, void **outData);
	 * <p>
	 * where
	 * uname = username
	 * pword = password
	 * inData = arbitrary data passed to the callback via the library if
	 * inData was set via NSLSetCredentials.  Can be NULL
	 * authToken = an authentication token that may be passed back to the
	 * library.  It is unused in straight callback authentication.
	 * outData = arbitrary data that may be passed back to the library from
	 * the callback.  It is available via NSLGetCredentials.
	 * outDataSize = the size in bytes of the outData
	 * <p>
	 * If the return from the callback is >= 0, the library will assume that
	 * authentication has suceeded. If the return is < 0, the library will assume
	 * that authentication has failed.  The return value of the callback is
	 * available via NSLGetCredentials.
	 * <p>
	 * In the third method of authentication the callback provides not just a
	 * return value but also an authentication token.  The authentication token
	 * is passed to Nalpeiron for all licensing requests and will be verified
	 * there before the licensing occurs.  One method of using the authentication
	 * token would be to generate a random string for the token during the
	 * callback.  Before returning this string to the library, Nalpeiron web
	 * services would be used to push to Nalpeiron.
	 * <p>
	 * NSLSetCredentials may only be used when
	 * <p>
	 * <p>
	 * There is no valid license (ie license status <= 0) or
	 * May be be used to set the callback and inData when accessing a license
	 * file with cached credentials. That is, the license was not returned when
	 * the library was closed and so the license file contains a valid (license
	 * status > 0) license with cached ABL credentials.
	 */

	public int callNSLSetCredentials(String classname, String methname,
									 String username, String password, String inData)
			throws NalpError {
		int i;

		try {
			i = NSLSetCredentials(
					(classname + '\000').getBytes("UTF-8"),
					(methname + '\000').getBytes("UTF-8"),
					(username + '\000').getBytes("UTF-8"),
					(password + '\000').getBytes("UTF-8"),
					(inData + '\000').getBytes("UTF-8"), offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}


	/**
	 * @param ssoToken A valid NULL terminated jwt presented as a string.  The jwt
	 *                 will be validated before being used as an authentication source.  The
	 *                 standard JWT validation method is used which will require access to the
	 *                 IDP for jwks information.  A local IDP may be used.
	 * @param inData   used at this time.  Should be set to NULL by the caller.
	 * @return = 0 If the call succeeded
	 * @return < 0 A negative error value is returned (\ref V10ERROR)
	 * @brief This function mirrors the functionality of NSLSetCredentials but
	 * uses a JWT (JSON Web Token) to gather the needed authentication information.
	 **/

	public int callNSLSetCredentialsSSO(String ssoToken, String inData)
			throws NalpError {
		int[] expEpoch = new int[1];
		int i;

		i = NSLSetCredentialsSSO(
				(ssoToken + '\000').getBytes(StandardCharsets.UTF_8), expEpoch,
				(inData + '\000').getBytes(StandardCharsets.UTF_8), offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		} else {
			i = expEpoch[0];
		}

		return i;
	}

	/**
	 * @return = 0 If the call succeeded
	 * @return < 0 A negative error value is returned (\ref V10ERROR)
	 * @brief Validate credentials passed into the library with NSLSetCredentials
	 * against any stored in the license file. When a program with an ABL license
	 * has been shutdown without a ReturnLicense or a ClearCredentials the
	 * credentials are cached in the license file. Once the library is restarted
	 * before the license file's entitlements can be accessed those credentials
	 * must be verified.  This state is indicated by a -70 license status. The
	 * verification is done using this call.
	 * <p>
	 * If the cached credentials are a username/password combination that was
	 * initially validated by the Nalpeiron server then the username and password
	 * passed to NSLSetCredentials will be validated against those stored in the
	 * license file.  NOTE the password is never stored or transmitted in
	 * plain text.
	 * <p>
	 * If the cached credentials were initially verified by a callback function
	 * then that callback will be called to verify the password and username
	 * passed to NSLSetCredentials.
	 * <p>
	 * If the cache credentials were initially verified by a callback with
	 * authentication token then that token will need to be passed back to the
	 * library from the callback function. The username and token will be verified
	 * against those stored in the license file.
	 */
	public int callNSLCheckCredentials()
			throws NalpError {
		int i;


		i = NSLCheckCredentials(offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * @param creds An array of strings that return the following values in order
	 *              <p>
	 *              lastRet a pointer to a 64 bit signed integer that will contain
	 *              the last return value from a callback provided to NSLSetCredentials.
	 *              <p>
	 *              username contains the last username set with NSLSetCredentials.
	 *              username must be cleared with a call to NSLFree.
	 *              <p>
	 *              inData the last data sent to the callback.
	 *              inData must be cleared with a call to NSLFree.
	 *              <p>
	 *              outData the last data received from the callback.
	 *              outData must be cleared with a call to NSLFree.
	 * @return = 0 If the call succeeded
	 * @return < 0 A negative error value is returned (\ref V10ERROR)
	 * @brief Get information from the last SetCredentials (or callback) call.
	 */
	public int callNSLGetCredentials(String[] creds)
			throws NalpError {
		int i;
		int[] lastRet = new int[1];
		byte[] username = new byte[256];
		byte[] inData = new byte[256];
		byte[] outData = new byte[256];
		String tmpVal;


		i = NSLGetCredentials(lastRet, username, inData, outData, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		tmpVal = String.valueOf(lastRet[0]);
		creds[0] = tmpVal;

		try {
			tmpVal = new String(username, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		creds[1] = tmpVal;

		try {
			tmpVal = new String(inData, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		creds[2] = tmpVal;

		try {
			tmpVal = new String(outData, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		creds[3] = tmpVal;

		return i;
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief: Clear credentials set for use with Nalpeiron server and named license
	 * @return 0 on success
	 */
	public int callNSLClearCredentials()
			throws NalpError {
		int i;


		i = NSLClearCredentials(offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		return i;
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief: Call to get a new license. Available only on certain platforms.
	 * @return 0 on success.
	 */
	public String callNSLGetNewLicenseCode(String profile)
			throws NalpError {
		byte[] nslLicCode = new byte[256];
		int i;

		try {
			i = NSLGetNewLicenseCode((profile + '\000').getBytes("UTF-8"), nslLicCode, offset);
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			return new String(nslLicCode, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief: Call to get a preset messages. Available only on certain platforms.
	 * @return 0 on success.
	 */
	public String callNSLGetPreset(int messageNo)
			throws NalpError {
		byte[] message = new byte[512];
		int i;


		i = NSLGetPreset(messageNo, message, offset);

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		try {
			return new String(message, 0, i, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new NalpError(-9006, "Invalid Encoding");
		}
	}

	/**
	 * @throws NalpError: If there was a problem calling the NSL function,
	 *                    this error will be thrown (\ref V10ERROR)
	 * @brief: Call to get a current messages. Available only on certain platforms.
	 * @return 0 on success.
	 */
	public ArrayList<String> callNSLGetMsgByDate()
			throws NalpError {
		byte[] message = new byte[512];
		int[] retVal = new int[1];
		int i = 0;
		String messStr = null;
		ArrayList<String> messArray;


		messArray = NSLGetMsgByDate(retVal, message, offset);

		i = retVal[0];

		if (i < 0) {
			throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
		}

		//Nalp library uses UTF-8 internally
		//for (Iterator<String> it = messArray.iterator(); it.hasNext();)
		//{
		//	messStr = it.next();
		//}

		return messArray;
	}
}

//end of NSLJAVA

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
