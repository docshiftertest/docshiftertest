package com.docshifter.core.utils.nalpeiron;

import com.docbyte.utils.Logger;
import com.docshifter.core.exceptions.DocShifterLicenceException;
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
    public void run() {
        validateLicenceStatus();
    }

    public final void validateLicenceStatus() {
        try {

            boolean validLicense = false;

            NalpeironHelper.LicenseStatus licenceStatus = nalpeironHelper.getLicenseStatus();
            //log status
            Logger.info("NALPlicence validation returned status: " + licenceStatus.toString(), null);

            //if the license status ha s value below 0, then the current license could not validate, try getting a new one
            if (licenceStatus.getValue() < 0) {
                //test for online connection
                boolean hasConnection = true;
                try {
                    nalpeironHelper.testNalpeironLicencingConnection();
                } catch (DocShifterLicenceException e) {
                    hasConnection = false;
                }

                if (hasConnection) {
                    // DO ONLINE CHECKING

                    // get a license with the supplied license code
                    licenceStatus = nalpeironHelper.getLicense(licenseNo, ""); //TODO define what has to happen with the XML REG INFO

                    //if the license status ha s value below 0, then the current license could not validate, try getting a new one
                    if (licenceStatus.getValue() < 0) {
                        // license activation failed.
                        Logger.info("The license could not be activated online, or your trial has expired", null);
                        validLicense = false;
                    } else {
                        Logger.info("The license has been activated online, or your trial is active", null);
                        validLicense = true;
                    }
                } else {
                    //DO OFFLINE CHECKING
                    String activationAnswer = nalpeironHelper.resolveLicenseActivationAnswer();

                    if (!StringUtils.isBlank(activationAnswer)) {
                       licenceStatus = nalpeironHelper.importCertificate(licenseNo, activationAnswer);

                        //if the license status ha s value below 0, then the current license could not validate, try getting a new one
                        if (licenceStatus.getValue() < 0) {
                            // license import failed.
                            Logger.info("The license could not be imported", null);
                            validLicense = false;
                        } else {
                            Logger.info("The license has been imported", null);
                            validLicense = true;
                        }
                    } else {
                       String activationRequest = nalpeironHelper.resolveLicenseActivationRequest();

                       //if the current activation request is empty, or the file does not exist create it
                        if (StringUtils.isBlank(activationRequest)) {
                            activationRequest = nalpeironHelper.getActivationCertificateRequest(licenseNo, ""); //TODO define what has to happen with the XML REG INFO

                            nalpeironHelper.writeLicenseActivationRequest(activationRequest);
                        }

                       validLicense = false;
                    }
                }
            } else {
                validLicense = true;
            }

            if (validLicense) {
                //process extra fields after validationg license
                String cpus = nalpeironHelper.getUDFValue("cpus");
                String testfield = nalpeironHelper.getUDFValue("testfield");
            } else {
                // license could not be validate, close application
                Logger.info("license could not be validated, closing application", null);
                SpringApplication.exit(nalpeironHelper.getApplicationContext());
            }

        } catch (DocShifterLicenceException ex) {
            Logger.fatal("Exception while trying to validate the nalpeiron license, closing the application", ex);
            SpringApplication.exit(nalpeironHelper.getApplicationContext());
        }
    }
}