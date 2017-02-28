package com.docshifter.core.utils.nalpeiron;

import com.docbyte.utils.Logger;
import com.docshifter.core.config.service.NalpeironService;
import com.docshifter.core.exceptions.DocShifterLicenceException;
import com.documentum.fc.client.impl.docbase.DocbaseCharSetUtility;
import com.nalpeiron.nalplibrary.NSL;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class NalpeironLicenseValidator implements Runnable {

    private final NalpeironHelper nalpeironHelper;

    public NalpeironLicenseValidator(NalpeironHelper nalpeironHelper) {
        this.nalpeironHelper = nalpeironHelper;
    }

    @Override
    public void run() {
        validateLicenceStatus();
    }

    public final void validateLicenceStatus() {
        NalpeironHelper.LicenseStatus licenceStatus = nalpeironHelper.getLicenseStatus();

        //if we do not have a valid licence, try to get a trail
        if (!NalpeironService.VALID_LICENCE_STATUS.contains(licenceStatus)) {
            //Send an empty license number for a trial and reg info
                licenceStatus = nalpeironHelper.getLicense("", "");

        }

        //if still do not have a valid licence, exit the app
        if (!NalpeironService.VALID_LICENCE_STATUS.contains(licenceStatus)) {
            Logger.fatal("license could not be validate. The licence status is: " + licenceStatus, null);
            SpringApplication.exit(nalpeironHelper.getApplicationContext()); //TODO; define error code
        }

        //TODO: if we still do not have a valid licence, try to get an activation certificate request (ofline activation supported?)
            /*if (!NalpeironService.NalpeironStatusCodes.VALID_LICENSE_CODES.contains(NalpeironStatusCodes)) {
                //Send an empty license number for a trial and reg info
                NalpeironStatusCodes = nsl.callNSLGetLicense("", "");
            SpringApplication.exit(applicationContext); //TODO; define error code
            }*/
    }
}