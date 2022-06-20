package com.docshifter.core.exceptions;

import com.nalpeiron.NalpError;


public class DocShifterLicenseException extends Exception {

    //	This will store the error code returned by the NALP function
    private	int			nalpErrorCode;
    private String		nalpErrorMsg;


    public DocShifterLicenseException(NalpError nalpError) {
        super(nalpError);

        this.nalpErrorCode = nalpError.getErrorCode();
        this.nalpErrorMsg = nalpError.getErrorMessage();
    }

    public DocShifterLicenseException(String arg0) {
        super(arg0);
    }


    public DocShifterLicenseException(Throwable arg0) {
        super(arg0);
    }


    public DocShifterLicenseException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }


    public int getNalpErrorCode() {
        return nalpErrorCode;
    }

    public String getNalpErrorMsg() {
        return nalpErrorMsg;
    }
}
