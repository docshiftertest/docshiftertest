//
// nsl.java
//
// Created July 19 2012
// R. D. Ramey
//

package com.nalpeiron.nalplibrary;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class NSL {
    private NALP nalp;

    //Security offset. Set it in constructor so we can adjust
    // returns from NSL functions
    private int offset;

    private String licenseNo;


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

    private native int NSLGetRefreshExpDate(long LibHandle,
                                            byte[] expDate, int offset);

    private native int NSLGetRefreshExpSec(long LibHandle,
                                           int[] expSec, int[] expEpoch, int offset);

    private native int NSLGetSubExpDate(long LibHandle,
                                        byte[] expDate, int offset);

    private native int NSLGetSubExpSec(long LibHandle,
                                       int[] expSec, int[] expEpoch, int offset);

    private native int NSLGetMaintExpDate(long LibHandle,
                                          byte[] expDate, int offset);

    private native int NSLGetMaintExpSec(long LibHandle,
                                         int[] expSec, int[] expEpoch, int offset);

    private native int NSLGetTrialExpDate(long LibHandle,
                                          byte[] expDate, int offset);

    private native int NSLGetTrialExpSec(long LibHandle,
                                         int[] expSec, int[] expEpoch, int offset);

    private native int NSLGetLicenseCode(long LibHandle,
                                         byte[] licCode, int offset);

    private native int NSLGetLicenseStatus(long LibHandle,
                                           int[] licStat, int offset);

    private native int NSLGetLicenseInfo(long LibHandle,
                                         int[] licenseType, int[] actType, int offset);

    private native int NSLGetNumbAvailProc(long LibHandle,
                                           int[] maxProc, int[] availProc, int offset);

    private native int NSLGetTimeStamp(long LibHandle,
                                       int[] timeStamp, int offset);

    private native int NSLGetFeatureStatus(long LibHandle,
                                           byte[] featureName, int[] fStat, int offset);

    private native int NSLCheckoutFeature(long LibHandle,
                                          byte[] featureName, byte[] licCode, int[] fStat, int offset);

    private native int NSLReturnFeature(long LibHandle,
                                        byte[] featureName, byte[] licCode, int offset);

    private native int NSLGetPoolStatus(long LibHandle,
                                        byte[] PoolName, int[] pAmt, int[] pStat, int offset);

    private native int NSLCheckoutPool(long LibHandle, byte[] poolName,
                                       byte[] licCode, int amt, int[] pStat, int offset);

    private native int NSLReturnPool(long LibHandle,
                                     byte[] poolName, byte[] licCode, int amt, int offset);

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

    private native int NSLRegister(long LibHandle,
                                   byte[] licenseNo, byte[] xmlRegInfo, int offset);

    private native int NSLTestConnection(long LibHandle, int offset);

    private native int NSLGetNewLicenseCode(long LibHandle,
                                            byte[] profile, byte[] licCode, int offset);

    private native int NSLGetPreset(long LibHandle,
                                    int messNo, byte[] message, int offset);

    private native ArrayList<String> NSLGetMsgByDate(long LibHandle,
                                                     int[] retVal, byte[] message, int offset);

    //Use this so we don't have to keep track of the library or
    // its handle.

    public NSL(
            NALP nalp,
            int oset
    ) {
        this.nalp = nalp;
        this.offset = oset;
    }

    public void
    NSLSetLicNo(
            String licNo
    ) {
        licenseNo = licNo;
    }

    public String
    NSLGetLicNo(
    ) {
        if (licenseNo == null) {
            return "";
        }

        return licenseNo;
    }

    /**
     * Call NSLGetVersion()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, nslVersion
     */
    public String callNSLGetVersion() throws NalpError {
        byte[] nslVersion = new byte[256];
        int i;


        i = NSLGetVersion(nalp.LibHandle, nslVersion, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslVersion, 0, nslVersion.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }

    /**
     * Call NSLGetComputerID()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, computerID
     */
    public String
    callNSLGetComputerID()
            throws NalpError {
        byte[] nslCompID = new byte[50];
        int i;


        i = NSLGetComputerID(nalp.LibHandle, nslCompID, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslCompID, 0, nslCompID.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }

    /**
     * Call NSLGetHostName()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, hostname
     */
    public String
    callNSLGetHostName()
            throws NalpError {
        byte[] nslHostName = new byte[128];
        int i;


        i = NSLGetHostName(nalp.LibHandle, nslHostName, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslHostName, 0, nslHostName.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }

    /**
     * Call NSLGetLeaseExpSec() -- Send any cache file to Nalp server
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, exp epoch seconds
     */
    public int
    callNSLGetLeaseExpSec()
            throws NalpError {
        int[] expSec = new int[1];
        int[] expEpoch = new int[1];
        int i;


        i = NSLGetLeaseExpSec(nalp.LibHandle, expSec, expEpoch, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return expSec[0];
    }

    /**
     * Call NSLGetLeaseExpDate()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, Expiration date
     */
    public String
    callNSLGetLeaseExpDate()
            throws NalpError {
        byte[] nslExpDate = new byte[128];
        int i;


        i = NSLGetLeaseExpDate(nalp.LibHandle, nslExpDate, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslExpDate, 0, nslExpDate.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }

    /**
     * Call NSLGetRefreshExpSec() -- Send any cache file to Nalp server
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, exp epoch seconds
     */
    public int
    callNSLGetRefreshExpSec()
            throws NalpError {
        int[] expSec = new int[1];
        int[] expEpoch = new int[1];
        int i;


        i = NSLGetRefreshExpSec(nalp.LibHandle, expSec, expEpoch, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return expSec[0];
    }

    /**
     * Call NSLGetRefreshExpDate()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, Expiration date
     */
    public String
    callNSLGetRefreshExpDate()
            throws NalpError {
        byte[] nslExpDate = new byte[128];
        int i;


        i = NSLGetRefreshExpDate(nalp.LibHandle, nslExpDate, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslExpDate, 0, nslExpDate.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }


    /**
     * Call NSLGetSubExpSec() -- Send any cache file to Nalp server
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, exp epoch seconds
     */
    public int
    callNSLGetSubExpSec()
            throws NalpError {
        int[] expSec = new int[1];
        int[] expEpoch = new int[1];
        int i;


        i = NSLGetSubExpSec(nalp.LibHandle, expSec, expEpoch, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return expSec[0];
    }

    /**
     * Call NSLGetSubExpDate()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, Expiration date
     */
    public String
    callNSLGetSubExpDate()
            throws NalpError {
        byte[] nslExpDate = new byte[128];
        int i;


        i = NSLGetSubExpDate(nalp.LibHandle, nslExpDate, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslExpDate, 0, nslExpDate.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }


    /**
     * Call NSLGetMaintExpSec() -- Send any cache file to Nalp server
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, exp epoch seconds
     */
    public int
    callNSLGetMaintExpSec()
            throws NalpError {
        int[] expSec = new int[1];
        int[] expEpoch = new int[1];
        int i;


        i = NSLGetMaintExpSec(nalp.LibHandle, expSec, expEpoch, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return expSec[0];
    }

    /**
     * Call NSLGetMaintExpDate()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, Expiration date
     */
    public String
    callNSLGetMaintExpDate()
            throws NalpError {
        byte[] nslExpDate = new byte[128];
        int i;


        i = NSLGetMaintExpDate(nalp.LibHandle, nslExpDate, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslExpDate, 0, nslExpDate.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }


    /**
     * Call NSLGetTrialExpSec() -- Send any cache file to Nalp server
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, exp epoch seconds
     */
    public int
    callNSLGetTrialExpSec()
            throws NalpError {
        int[] expSec = new int[1];
        int[] expEpoch = new int[1];
        int i;


        i = NSLGetTrialExpSec(nalp.LibHandle, expSec, expEpoch, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return expSec[0];
    }

    /**
     * Call NSLGetTrialExpDate()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, Expiration date
     */
    public String
    callNSLGetTrialExpDate()
            throws NalpError {
        byte[] nslExpDate = new byte[128];
        int i;


        i = NSLGetTrialExpDate(nalp.LibHandle, nslExpDate, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslExpDate, 0, nslExpDate.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }

    /**
     * Call NSLGetLicense
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error
     */
    public int
    callNSLGetLicense(String licenseNo, String xmlRegInfo)
            throws NalpError {
        int[] licStat = new int[1];
        int i;


        try {
            i = NSLGetLicense(nalp.LibHandle,
                    (licenseNo + '\000').getBytes("UTF-8"),
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
     * Call NSLReturnLicense
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error
     */
    public int
    callNSLReturnLicense(String licenseNo)
            throws NalpError {
        int[] licStat = new int[1];
        int i;


        try {
            i = NSLReturnLicense(nalp.LibHandle,
                    (licenseNo + '\000').getBytes("UTF-8"), licStat, offset);
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return licStat[0];
    }

    /**
     * Call NSLImportCertificate
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error
     */
    public int
    callNSLImportCertificate(String licenseNo, String cert)
            throws NalpError {
        int[] licStat = new int[1];
        int i;

        try {
            i = NSLImportCertificate(nalp.LibHandle,
                    (cert + '\000').getBytes("UTF-8"),
                    (licenseNo + '\000').getBytes("UTF-8"), licStat, offset);
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return licStat[0];
    }

    /**
     * Call NSLGetActivationCertReq()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, nslVersion
     */
    public String
    callNSLGetActivationCertReq(String licenseNo, String xmlRegInfo)
            throws NalpError {
        byte[] nslCert = new byte[4096];
        int i;


        try {
            i = NSLGetActivationCertReq(nalp.LibHandle,
                    (licenseNo + '\000').getBytes("UTF-8"),
                    (xmlRegInfo + '\000').getBytes("UTF-8"), nslCert, offset);
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslCert, 0, nslCert.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }


    /**
     * Call NSLGetDeactivationCertReq()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, nslVersion
     */
    public String
    callNSLGetDeactivationCertReq(String licenseNo)
            throws NalpError {
        byte[] nslCert = new byte[4096];
        int i;


        try {
            i = NSLGetDeactivationCertReq(nalp.LibHandle,
                    (licenseNo + '\000').getBytes("UTF-8"), nslCert, offset);
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslCert, 0, nslCert.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }


    /**
     * Call NSLValidateLibrary() - Collect and send system nalpation to Napeiron
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, anything else in an invalid library
     */
    public int
    callNSLValidateLibrary(int custID, int prodID)
            throws NalpError {
        int i;


        i = NSLValidateLibrary(nalp.LibHandle, custID, prodID, offset);

        if (i < 0) {
            System.out.println("Error loading library: %d" + i);
            //throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return i;
    }

    /**
     * Call NSLGetLicenseStatus()
     *
     * @throws NalpError :	If there was a problem calling the NSLfunction,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, license status
     */
    public int callNSLGetLicenseStatus()
            throws NalpError {
        int[] licStat = new int[1];
        int i;


        i = NSLGetLicenseStatus(nalp.LibHandle, licStat, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return licStat[0];
    }

    /**
     * Call NSLGetLicenseInfo() to get license type
     *
     * @throws NalpError :	If there was a problem calling the NSLfunction,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, license type
     */
    public int callNSLGetLicenseType()
            throws NalpError {
        int[] licType = new int[1];
        int[] actType = new int[1];
        int i;


        i = NSLGetLicenseInfo(nalp.LibHandle, licType, actType, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return licType[0];
    }

    /**
     * Call NSLGetLicenseInfo() to get activation type
     *
     * @throws NalpError :	If there was a problem calling the NSLfunction,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, license type
     */
    public int callNSLGetActivationType()
            throws NalpError {
        int[] licType = new int[1];
        int[] actType = new int[1];
        int i;


        i = NSLGetLicenseInfo(nalp.LibHandle, licType, actType, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return actType[0];
    }

    /**
     * Call NSLGetNumbAvailProc() to get maximum allowed processes
     *
     * @throws NalpError :	If there was a problem calling the NSLfunction,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, license type
     */
    public int callNSLGetMaxProcs()
            throws NalpError {
        int[] maxProc = new int[1];
        int[] availProc = new int[1];
        int i;


        i = NSLGetNumbAvailProc(nalp.LibHandle, maxProc, availProc, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return maxProc[0];
    }

    /**
     * Call NSLGetNumbAvailProc() to get maximum allowed processes
     *
     * @throws NalpError :	If there was a problem calling the NSLfunction,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, license type
     */
    public int callNSLGetAvailProcs()
            throws NalpError {
        int[] maxProc = new int[1];
        int[] availProc = new int[1];
        int i;


        i = NSLGetNumbAvailProc(nalp.LibHandle, maxProc, availProc, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return availProc[0];
    }

    /**
     * Call NSLGetLicenseCode()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, license code
     */
    public String callNSLGetLicenseCode()
            throws NalpError {
        byte[] nslLicCode = new byte[256];
        int i;


        i = NSLGetLicenseCode(nalp.LibHandle, nslLicCode, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslLicCode, 0, nslLicCode.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }

    /**
     * Call NSLGetTimeStamp()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error
     */
    public int
    callNSLGetTimeStamp()
            throws NalpError {
        int[] timeStamp = new int[1];
        int i;


        i = NSLGetTimeStamp(nalp.LibHandle, timeStamp, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return timeStamp[0];
    }

    /**
     * Call NSLGetFeatureStatus() -- Get status of floating feature
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, feature status
     */
    public int callNSLGetFeatureStatus(String featureName)
            throws NalpError {
        int[] featureStatus = new int[1];
        int i;


        try {
            i = NSLGetFeatureStatus(nalp.LibHandle,
                    (featureName + '\000').getBytes("UTF-8"),
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
     * Call NSLCheckoutFeature() -- Checkout a floating feature
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, feature status
     */
    public int callNSLCheckoutFeature(String featureName, String licenseNo)
            throws NalpError {
        int[] featureStatus = new int[1];
        int i;


        try {
            i = NSLCheckoutFeature(nalp.LibHandle,
                    (featureName + '\000').getBytes("UTF-8"),
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
     * Call NSLReturnFeature() -- Return a floating feature to server
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, feature status
     */
    public int callNSLReturnFeature(String featureName, String licenseNo)
            throws NalpError {
        int i;


        try {
            i = NSLReturnFeature(nalp.LibHandle,
                    (featureName + '\000').getBytes("UTF-8"),
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
     * Call NSLGetPoolStatus() -- Get status and number of elements in pool
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error. Returns
     * pool status if invalid else returns number of element checked out
     */
    public int callNSLGetPoolStatus(String poolName)
            throws NalpError {
        int[] poolStatus = new int[1];
        int[] poolAmt = new int[1];
        int i;


        try {
            i = NSLGetPoolStatus(nalp.LibHandle,
                    (poolName + '\000').getBytes("UTF-8"),
                    poolAmt, poolStatus, offset);
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
     * Call NSLCheckoutPool() -- Checkout elements from a pool
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error else number of elements
     */
    public int callNSLCheckoutPool(String poolName, String licenseNo, int amt)
            throws NalpError {
        int[] poolStatus = new int[1];
        int i;


        try {
            //the library will vet the amt.
            i = NSLCheckoutPool(nalp.LibHandle,
                    (poolName + '\000').getBytes("UTF-8"),
                    (licenseNo + '\000').getBytes("UTF-8"),
                    amt, poolStatus, offset);
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        if (poolStatus[0] < 0) {
            return poolStatus[0];
        }

        return poolStatus[0];
    }

    /**
     * Call NSLReturnPool() -- Return elements to pool
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, feature status
     */
    public int callNSLReturnPool(String poolName, String licenseNo, int amt)
            throws NalpError {
        int i;


        try {
            //the library will vet the amt.
            i = NSLReturnPool(nalp.LibHandle,
                    (poolName + '\000').getBytes("UTF-8"),
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
     * Call NSLGetUDFValue()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, Expiration date
     */
    public String
    callNSLGetUDFValue(String UDFName)
            throws NalpError {
        byte[] UDFValue = new byte[4096];
        int i;


        try {
            i = NSLGetUDFValue(nalp.LibHandle,
                    (UDFName + '\000').getBytes("UTF-8"),
                    UDFValue, offset);
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(UDFValue, 0, UDFValue.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }

    /**
     * Call NSLGetNumbAvailProc()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, license status
     */
    public int
    callNSLGetNumbAvailProc(int[] maxProc, int[] availProc)
            throws NalpError {
        int i;


        i = NSLGetNumbAvailProc(nalp.LibHandle, maxProc, availProc, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return i;
    }

    /**
     * Call NSLRegister
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error
     */
    public int callNSLRegister(String licenseNo, String xmlRegInfo)
            throws NalpError {
        int i;


        try {
            i = NSLRegister(nalp.LibHandle,
                    (licenseNo + '\000').getBytes("UTF-8"),
                    (xmlRegInfo + '\000').getBytes("UTF-8"), offset);
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return i;
    }

    public int callNSLTestConnection()
            throws NalpError {
        int i;


        i = NSLTestConnection(nalp.LibHandle, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        return i;
    }

    /**
     * Call NSLGetNewLicenseCode()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, license code
     */
    public String callNSLGetNewLicenseCode(String profile)
            throws NalpError {
        byte[] nslLicCode = new byte[256];
        int i;

        try {
            i = NSLGetNewLicenseCode(nalp.LibHandle,
                    (profile + '\000').getBytes("UTF-8"), nslLicCode, offset);
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(nslLicCode, 0, nslLicCode.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }

    /**
     * Call NSLGetPreset()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, preset message
     */
    public String callNSLGetPreset(int messageNo)
            throws NalpError {
        byte[] message = new byte[512];
        int i;


        i = NSLGetPreset(nalp.LibHandle, messageNo, message, offset);

        if (i < 0) {
            throw new NalpError(i, nalp.callNalpGetErrorMsg(i));
        }

        //Nalp library uses UTF-8 internally
        try {
            return new String(message, 0, message.length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }
    }

    /**
     * Call NSLGetMsgByDate()
     *
     * @throws NalpError :	If there was a problem calling the NSL function,
     *                   this error will be thrown
     * @return: 0 on success, negative value on error, preset message
     */
    public ArrayList<String> callNSLGetMsgByDate()
            throws NalpError {
        byte[] message = new byte[512];
        int[] retVal = new int[1];
        int i = 0;
        String messStr = null;
        ArrayList<String> messArray;


        messArray = NSLGetMsgByDate(nalp.LibHandle,
                retVal, message, offset);

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

/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
