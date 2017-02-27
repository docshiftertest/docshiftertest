package com.docshifter.core.config.service;

import com.docbyte.utils.Logger;
import com.docshifter.core.exceptions.DocShifterLicenceException;
import com.nalpeiron.nalplibrary.NALP;
import com.nalpeiron.nalplibrary.NSA;
import com.nalpeiron.nalplibrary.NSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class NalperionService {

    //These private ints are unique to your product and must
    // be set here to the values corresponding to your product.
    private static final int customerID = 4854;
    private static final int productID = 102;
    private static final int AUTH_X = 375;
    private static final int AUTH_Y = 648;
    private static final int AUTH_Z = 263;
    private static final String CLIENT_DATA = "";

    public static final String NALPERION_USERNAME = "";

    private static NALP nalp;
    private static NSA nsa;
    private static NSL nsl;

    private static final long[] aid = {0L};

    private final ScheduledExecutorService licenceValidationScheduler = Executors.newSingleThreadScheduledExecutor();

    private ApplicationContext applicationContext;

    //TODO: LOGGING
    @Autowired
    public NalperionService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        openValidateNalperionLibrary();
        licenceValidationScheduler.scheduleAtFixedRate(new NalperionLicenseValidator(), 0, 1, TimeUnit.HOURS);
    }


    //TODO LOGGING
    private final void openValidateNalperionLibrary() {
        //generate a random number between 1 and 500 and use
        // it to calculate the security offset
        int security = 1 + (int) (Math.random() * (501));
        int offset = AUTH_X + ((security * AUTH_Y) % AUTH_Z);

        //Library open, close and error handling
        nalp = new NALP();

        //Analytics functions
        nsa = new NSA(nalp);

        //Licensing functions
        nsl = new NSL(nalp, offset);

        //Open and Initialize library
        //
        //Parameters for this call are
        //		String Filename, boolean NSAEnabled,
        //		boolean NSLEnabled, int LogLevel, String WorkDir,
        //		int LogQLen, int CacheQLen, int NetThMin,
        //		int NetThMax, int OfflineMode, String ProxyIP,
        //		String ProxyPort, String ProxyUsername,
        //		String ProxyPass, int security)
        //TODO: get config
        nalp.callNalpLibOpen("./bin/docShifterFileCheck.dll", true, true,
                4, "", 300, 35, 1, 10, 0, "", "", "", "", security);

        //Turn end user privacy off. Any error will be caught
        // in callNSASetPrivacy.
        nsa.callNSASetPrivacy(0);

        //Validate the library.  This ensures that the library
        // is a genuine Nalpeiron library and was stamped with
        // the correct data.  THIS STEP IS VERY IMPORTANT.  Without
        // validation and the security offsets, it would be possible
        // to spoof a library that always returned OK.
        if (0 != nsl.callNSLValidateLibrary(customerID, productID)) {
            Logger.fatal("Invalid or corrupt licensing library, terminating software execution", null);
            SpringApplication.exit(applicationContext); //TODO; define error code
        }

        //At thid point we have a license, so start analytics
        nsa.callNSAApStart(CLIENT_DATA, aid);
    }

    public long[] validateAndStartModule(String moduleId, long[] fid) throws  DocShifterLicenceException {
        int featureStatusCode = nsl.callNSLGetFeatureStatus(moduleId);

        //return < 0 is error. return = 0 means feature disabled
        if (!NalperionStatusCodes.VALID_FEATURE_CODES.contains(featureStatusCode)) {
            String errorMessage = "feature could not be activated. The feature status is: " + featureStatusCode + ". Blocking acces to module: " + moduleId;
            DocShifterLicenceException ex = new DocShifterLicenceException(errorMessage);
            Logger.info(errorMessage, ex);
            throw ex;
        }

        //At this point we have access to the feature.  Send in
        // some analytics
        nsa.callNSAFeatureStart(NALPERION_USERNAME, moduleId, CLIENT_DATA, fid);

        return fid;
    }

    public void endModule(String moduleId, long[] fid) {
        //call end feature
        nsa.callNSAFeatureStop(NALPERION_USERNAME, moduleId, CLIENT_DATA, fid);
    }

    private class NalperionLicenseValidator implements Runnable {

        @Override
        public void run() {
            validateLicenceStatus();
        }

        private final void validateLicenceStatus() {
            int licenceStatus = nsl.callNSLGetLicenseStatus();

            //if we do not have a valid licence, try to get a trail
            if (!NalperionStatusCodes.VALID_LICENSE_CODES.contains(licenceStatus)) {
                //Send an empty license number for a trial and reg info
                licenceStatus = nsl.callNSLGetLicense("", "");
            }

            //if still do not have a valid licence, exit the app
            if (!NalperionStatusCodes.VALID_LICENSE_CODES.contains(licenceStatus)) {
                Logger.fatal("license could not be validate. The licence status is: " + licenceStatus, null);
                SpringApplication.exit(applicationContext); //TODO; define error code
            }

            //TODO: if we still do not have a valid licence, try to get an activation certificate request (ofline activation supported?)
            /*if (!NalperionService.NalperionStatusCodes.VALID_LICENSE_CODES.contains(NalperionStatusCodes)) {
                //Send an empty license number for a trial and reg info
                NalperionStatusCodes = nsl.callNSLGetLicense("", "");
            SpringApplication.exit(applicationContext); //TODO; define error code
            }*/
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();


        //Stop the licenceValidationScheduler
        licenceValidationScheduler.shutdown();

        //End analytics
        nsa.callNSAApStop(CLIENT_DATA, aid);

        //Flush the cache in case anything is queued up
        nsa.callNSASendCache(NALPERION_USERNAME);

        //Cleanup and shutdown library
        nalp.callNalpLibClose();
    }

    public static class NalperionStatusCodes {
        public static final int DAEMON_MASTER_LICENSE = 16;
        public static final int DAEMON_LTCO_LICENSE = 15;
        public static final int DAEMON_OEM_LICENSE = 14;
        public static final int CONCURRENT_LICENSE_ACTIVATED = 3;
        public static final int TRIAL_ACTIVATED = 2;
        public static final int VALID = 1;

        public static final int FEATURE_NOT_AUTHORISED = -3;
        public static final int FEATURE_PRODUCT_NOT_FOUND = -4;
        public static final int LICENSE_CANNOT_VERIFY = -5;
        public static final int LICENSE_RETURNED_TO_SERVER = -6;
        public static final int DATE_SET_BACK_TOO_FAR = -7;
        public static final int INVALID_PRODUCT_STATE = -8;
        public static final int LICENSEE_NOT_AVAIABLE = -50;
        public static final int DAEMON_VERIFICATION_FILED = -51;
        public static final int DAEMON_SYSTEM_ID_FAILURE = -52;
        public static final int INACTIVE_PRODUCT = -110;
        public static final int TRAIL_PERIOD_INVALID = -111;
        public static final int COMPUTERID_ALREADY_ACTIVE = -112;
        public static final int TRAIL_EXPIRED = -113;
        public static final int INACTIVE_LICENSE_CODE = -114;
        public static final int ALLOWED_ACTIVATIONS_EXCEEDED = -115;
        public static final int SUBSCRIPTION_EXPIRED = -116;
        public static final int DUPLICATE_DEVICE_ID = -117;
        public static final int NETWORK_ERROR = -200;
        public static final int SEAT_REVOKED = -201;

        public static final List VALID_LICENSE_CODES = new ArrayList(Arrays.asList(VALID, TRIAL_ACTIVATED));

        public static final List VALID_FEATURE_CODES = new ArrayList(Arrays.asList(VALID));
    }
}
