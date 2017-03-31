package com.docshifter.core.utils.nalpeiron;

import com.docbyte.utils.Logger;
import com.docshifter.core.exceptions.DocShifterLicenseException;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;

public class NalpeironLicenseValidator implements Runnable {

    private String licenseNo;

    private final NalpeironHelper nalpeironHelper;

    public NalpeironLicenseValidator(NalpeironHelper nalpeironHelper, String licenseNo) {
        this.licenseNo = licenseNo;
        this.nalpeironHelper = nalpeironHelper;
    }

    @Override
    public final void run() {
        validateLicenseStatus();
    }

    public final void validateLicenseStatus() {
        try {
            boolean validLicense = false;

            //test for online connection
            boolean hasConnection = true;
            try {
                nalpeironHelper.testNalpeironLicencingConnection();
            } catch (DocShifterLicenseException e) {
                hasConnection = false;
            }

            NalpeironHelper.LicenseStatus licenseStatus;

            if (hasConnection) {
                // ALWAYS DO ONLINE CHECKING WHEN HAVING CONNECTION

                // get a license with the supplied license code
                licenseStatus = nalpeironHelper.getLicense(licenseNo, ""); //TODO define what has to happen with the XML REG INFO

                //if the license status ha s value below 0, then the current license could not validate, try getting a new one
                if (licenseStatus.getValue() < 0) {
                    // license activation failed.
                    Logger.info("The license could not be activated online, or your trial has expired", null);
                    validLicense = false;
                } else {
                    Logger.info("The license has been activated online, or your trial is active", null);
                    validLicense = true;
                }
            } else {
                //DO OFFLINE CHECKING

                licenseStatus = nalpeironHelper.getLicenseStatus();
                //log status
                Logger.info("Current license status is: " + licenseStatus.toString(), null);

                //if the license status ha s value below 0, then the current license could not validate, try activating or generating an offline activation request
                if (licenseStatus.getValue() < 0) {
                    Logger.info("Invalid license. Trying to activate the license", null);
                    String activationAnswer = nalpeironHelper.resolveLicenseActivationAnswer();

                    if (!StringUtils.isBlank(activationAnswer)) {
                        licenseStatus = nalpeironHelper.importCertificate(licenseNo, activationAnswer);

                        //if the license status ha s value below 0, then the current license could not validate, try getting a new one
                        if (licenseStatus.getValue() < 0) {
                            // license import failed.
                            Logger.info("The license could not be activated offline, import of DSLicenseActivationAnswer.txt failed", null);
                            validLicense = false;
                        } else {
                            Logger.info("The license has been activated offline, import of DSLicenseActivationAnswer.txt successful", null);
                            validLicense = true;
                        }
                    } else {
                        String activationRequest = nalpeironHelper.resolveLicenseActivationRequest();

                        //if the current activation request is empty, or the file does not exist create it
                        if (StringUtils.isBlank(activationRequest)) {
                            activationRequest = nalpeironHelper.getActivationCertificateRequest(licenseNo, ""); //TODO define what has to happen with the XML REG INFO

                            nalpeironHelper.writeLicenseActivationRequest(activationRequest);
                        }

                        Logger.info("The license needs be activated offline, or you need to have an active internet connection. Activation request code written to DSLicenseActivationRequest.txt", null);
                        validLicense = false;
                    }
                } else {
                    validLicense = true;
                }
            }

            if (validLicense) {
                //process extra fields after validationg license
            } else {
                // license could not be validate, close application
                int errorCode = 0;//TODO: we need to exit with zero or yajsw will restart the service
                Logger.info("license could not be validated, closing application", null);
                SpringApplication.exit(nalpeironHelper.getApplicationContext(), () -> errorCode);

                Logger.debug("exited Spring app, doing system.exit()", null);

                System.exit(errorCode);
            }

        } catch (DocShifterLicenseException ex) {
            int errorCode = 0;//TODO: we need to exit with zero or yajsw will restart the service
            Logger.fatal("Exception while trying to validate the nalpeiron license, closing the application", ex);
            SpringApplication.exit(nalpeironHelper.getApplicationContext(), () -> errorCode);

            Logger.debug("exited Spring app, doing system.exit()", null);

            System.exit(errorCode);
        }
    }
}