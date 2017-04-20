//
//nalp.java
//
// Created Jan 19 2012
// R. D. Ramey

package com.nalpeiron.nalplibrary;

import java.io.UnsupportedEncodingException;

public class NALP {
    //Library handle for NSA/NSL library
    public long LibHandle;

    //Library open function
    private native int NalpLibOpen(byte[] Filename, byte[] xmlParams, long[] lhandle_t);

    //Library close function
    private native int NalpLibClose(long LibHandle);

    //Turn NSA/NSL error returns into nalpative strings
    private native String NalpGetErrorMsg(long LibHandle, int nalpErrNo);

    //Open the JNI wrapper library.  Use static initialization block
    // so that we only do this once no matter how man NALPs are created
    static {
        System.loadLibrary("nalpjava");
    }

    /**
     * Call NalpLibOpen(), which initializes the NSA C library for use
     *
     * @throws NalpError: If there was a problem calling the NSA function,
     *                    this error will be thrown
     * @return: 0 on success, negative value on error.  A (void *) cast
     * of the library handle in libHandle
     */
    public int callNalpLibOpen(String Filename, boolean NSAEnable, boolean NSLEnable, int LogLevel, String WorkDir, int LogQLen, int CacheQLen, int NetThMin, int NetThMax, int OfflineMode, String ProxyIP, String ProxyPort, String ProxyUsername, String ProxyPass, String DaemonIP, String DaemonPort, String DaemonUser, String DaemonPass, int security) throws NalpError {
        int i;
        long lhandle_t[];
        String xmlParams;


        lhandle_t = new long[1];

        //Construct NalpLibOpen's xml parameter
        xmlParams = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        xmlParams = xmlParams + "<SHAFERXMLParams>";

        //Default for each library (NSA and NSL) is disabled.  You
        // can enable one or the other or both.
        //
        // Enable NSA
        if (NSAEnable == true) {
            xmlParams = xmlParams + "<NSAEnabled>1</NSAEnabled>";
        }

        //Enable NSL
        if (NSLEnable == true) {
            xmlParams = xmlParams + "<NSLEnabled>1</NSLEnabled>";
        }

        xmlParams = xmlParams +
                "<SecurityValue>" + security + "</SecurityValue>";

        if (!WorkDir.equals("")) {
            xmlParams = xmlParams + "<WorkDir>" + WorkDir + "</WorkDir>";
        }

        if ((LogLevel < 0) || (LogLevel > 5)) {
            LogLevel = 0;
        }

        xmlParams = xmlParams + "<LogLevel>" + LogLevel + "</LogLevel>";

        if (LogQLen <= 0) {
            LogQLen = 300;
        }

        xmlParams = xmlParams + "<LogQLen>" + LogQLen + "</LogQLen>";

        if (CacheQLen <= 0) {
            CacheQLen = 25;
        }

        xmlParams = xmlParams + "<CacheQLen>" + CacheQLen + "</CacheQLen>";

        if ((NetThMin <= 0) || (NetThMin > NetThMax)) {
            NetThMin = 10;
        }

        xmlParams = xmlParams + "<SoapThMin>" + NetThMin + "</SoapThMin>";

        if ((NetThMax <= 0) || (NetThMax < NetThMin)) {
            NetThMax = 10;
        }

        xmlParams = xmlParams + "<SoapThMax>" + NetThMax + "</SoapThMax>";

        if ((OfflineMode != 0) && (OfflineMode != 1)) {
            OfflineMode = 0;
        }

        xmlParams = xmlParams + "<OfflineMode>" + OfflineMode + "</OfflineMode>";

        if (!ProxyIP.equals("")) {
            xmlParams = xmlParams + "<ProxyIP>" + ProxyIP + "</ProxyIP>";
        }

        if (!ProxyPort.equals("")) {
            xmlParams = xmlParams + "<ProxyPort>" + ProxyPort + "</ProxyPort>";
        }

        if (!ProxyUsername.equals("")) {
            xmlParams = xmlParams +
                    "<ProxyUsername>" + ProxyUsername + "</ProxyUsername>";
        }

        if (!ProxyPass.equals("")) {
            xmlParams = xmlParams +
                    "<ProxyPassword>" + ProxyPass + "</ProxyPassword>";
        }

        if (!DaemonIP.equals("")) {
            xmlParams = xmlParams +
                    "<DaemonIP>" + DaemonIP + "</DaemonIP>";
        }

        if (!DaemonPort.equals("")) {
            xmlParams = xmlParams +
                    "<DaemonPort>" + DaemonPort + "</DaemonPort>";
        }

        if (!DaemonUser.equals("")) {
            xmlParams = xmlParams +
                    "<DaemonUser>" + DaemonUser + "</DaemonUser>";
        }

        if (!DaemonPass.equals("")) {
            xmlParams = xmlParams +
                    "<DaemonPassword>" + DaemonPass + "</DaemonPassword>";
        }

        xmlParams = xmlParams + "</SHAFERXMLParams>";

        try {
            i = NalpLibOpen((Filename + '\000').getBytes("UTF-8"),
                    (xmlParams + '\000').getBytes("UTF-8"), lhandle_t);
        } catch (UnsupportedEncodingException e) {
            throw new NalpError(-9006, "Invalid Encoding");
        }

        if (i < 0) {
            //throw new nalpError(i, NalpGetErrorMsg(LibHandle, i));
            System.out.println("Error " + i + ": " + callNalpGetErrorMsg(i));
        } else {
            LibHandle = lhandle_t[0];
        }

        return i;
    }


    /**
     * Call NalpLibClose() which shuts down the C library
     *
     * @throws NalpError: If there was a problem calling the NSA function,
     *                    this error will be thrown
     * @return: 0 on success, negative value on error
     */
    public int callNalpLibClose() throws NalpError {
        int i;

        i = NalpLibClose(LibHandle);

        if (i < 0) {
            throw new NalpError(i, NalpGetErrorMsg(LibHandle, i));
        }

        return i;
    }


    /**
     * Call NalpGetErrorMsg(), Get error message associated with error no.
     *
     * @return: utf8 xml string containing error nalpation
     */
    public String callNalpGetErrorMsg(int nalpErrorNo) throws NalpError {
        String nalpErrorMsg;

        nalpErrorMsg = NalpGetErrorMsg(LibHandle, nalpErrorNo);

        return new String(nalpErrorMsg);
    }
}


/*
 * vim:tabstop=4
 * vim:shiftwidth=4
 */
