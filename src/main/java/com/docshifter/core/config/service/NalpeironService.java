package com.docshifter.core.config.service;

import com.docbyte.utils.Logger;
import com.docshifter.core.exceptions.DocShifterLicenceException;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import com.nalpeiron.nalplibrary.NALP;
import com.nalpeiron.nalplibrary.NSA;
import com.nalpeiron.nalplibrary.NSL;
import com.nalpeiron.nalplibrary.nalpError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class NalpeironService {

    //These private ints are unique to your product and must
    // be set here to the values corresponding to your product.
    private static final int customerID = 4854;
    private static final int productID = 102;
    private static final int AUTH_X = 375; // N{5...499}
    private static final int AUTH_Y = 648; // N{501...999}
    private static final int AUTH_Z = 263; // N{233...499}
    private static final String CLIENT_DATA = "";

    public static final String NALPEIRON_USERNAME = "";
    private static final String APP_LANGUAGE = "en-US";
    private static final String VERSION = "6.0-alpha";
    private static final String EDITION = "DEV";
    private static final String BUILD = "local";
    private static final String LICENCE_STAT = "???";

    private static NalpeironHelper helper;

    private static final long[] aid = {0L};

    private ApplicationContext applicationContext;

    public static final List<NalpeironHelper.FeatureStatus> VALID_FEATURE_STATUS = Arrays.asList(NalpeironHelper.FeatureStatus.AUTHORIZED);
    public static final List<NalpeironHelper.LicenseStatus> VALID_LICENCE_STATUS = Arrays.asList(NalpeironHelper.LicenseStatus.PROD_AUTHORIZED, NalpeironHelper.LicenseStatus.PROD_INTRIAL, NalpeironHelper.LicenseStatus.PROD_NETWORK, NalpeironHelper.LicenseStatus.PROD_NETWORK_LTCO);

    //TODO: LOGGING
    @Autowired
    public NalpeironService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        try {
            NalpeironHelper.dllTest();
        } catch (DocShifterLicenceException e) {
            Logger.fatal("nalpjava library could not be found", e);
            SpringApplication.exit(applicationContext); //TODO; define error code
        }
        openValidateNalpeironLibrary();
    }

    private final void openValidateNalpeironLibrary() {
        try {
            //generate a random number between 1 and 500 and use it to calculate the security offset
            int security = 1 + (int) (Math.random() * (501));
            int offset = AUTH_X + ((security * AUTH_Y) % AUTH_Z);

            //Library open, close and error handling
            NALP nalp = new NALP();

            //Analytics functions
            NSA nsa = new NSA(nalp);

            //Licensing functions
            NSL nsl = new NSL(nalp, offset);

            helper = new NalpeironHelper(applicationContext, nalp, nsa, nsl);

            { //TODO get config
                // Advanced settings, for normal operation leave as defaults
                int LogLevel = 6; // set log level, please see documentation
                int OfflineMode = 0;// Select Offline mode, please see documentation
                int LogQLen = 300; // please see documentation
                int CacheQLen = 35; // please see documentation
                int NetThMin = 25; // please see documentation
                int NetThMax = 25; // please see documentation
                String ProxyIP = ""; // InternetConnection Proxy IP Address if required
                String ProxyPort = ""; // InternetConnection Proxy Port if required
                String ProxyUsername = ""; // InternetConnection Proxy Username if required
                String ProxyPass = ""; // InternetConnection Proxy Password if required
                String DaemonIP = ""; //Daemon IP
                String DaemonPort = ""; //Daemon Port
                String DaemonUser = ""; //Daemon User
                String DaemonPass = ""; //Daemon Password
                Boolean NSAEnable = true; // Enable Analytics
                Boolean NSLEnable = true; // Enable Licensing
                String WorkDir = System.getenv("APPDATA") + "\\Nalpeiron"; // Work
                String LogDir = System.getenv("APPDATA") + "\\Nalpeiron";

                helper.openNalpLibriray("./bin/docShifterFileCheck.dll", NSAEnable, NSLEnable, LogLevel, WorkDir, LogQLen, CacheQLen, NetThMin, NetThMax, OfflineMode, ProxyIP, ProxyPort, ProxyUsername, ProxyPass, DaemonIP, DaemonPort, DaemonUser, DaemonPass, security);
            }

            //Turn end user privacy off
            helper.setAnalyticsPrivacy(NalpeironHelper.PrivacyValue.OFF.getValue());

            helper.validateLibrary(customerID, productID);

            helper.validateLicenceAndInitiatePeriodicChecking();

            //At this point we have a license, so start analytics
            helper.startAnalyticsApp(NALPEIRON_USERNAME, CLIENT_DATA, aid);
            helper.sendAnalyticsSystemInfo(NALPEIRON_USERNAME, APP_LANGUAGE, VERSION,
                    EDITION, BUILD, LICENCE_STAT, CLIENT_DATA);

        } catch (DocShifterLicenceException | nalpError e) {
            Logger.fatal("error inn docshifter licence processing", e);
            SpringApplication.exit(applicationContext); //TODO; define error code
        }
    }

    public long[] validateAndStartModule(String moduleId, long[] fid) throws DocShifterLicenceException {
        NalpeironHelper.FeatureStatus featureStatus = helper.getFeatureStatus(moduleId);

        if (!VALID_FEATURE_STATUS.contains(featureStatus)) {
            String errorMessage = "feature could not be activated. The feature status is: " + featureStatus.name() + ". Blocking acces to module: " + moduleId;
            DocShifterLicenceException ex = new DocShifterLicenceException(errorMessage);
            Logger.info(errorMessage, ex);
            throw ex;
        }

        //At this point we have access to the feature.  do some analytics
        helper.startFeature(NALPEIRON_USERNAME, moduleId, CLIENT_DATA, fid);

        return fid;
    }

    public void endModule(String moduleId, long[] fid) throws DocShifterLicenceException {
        //call end feature
        helper.stopFeature(NALPEIRON_USERNAME, moduleId, CLIENT_DATA, fid);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        //Stop the licenceValidationScheduler
        helper.stopLicenceValidationScheduler();

        //End analytics
        helper.stopAnalyticsApp(NALPEIRON_USERNAME, CLIENT_DATA, aid);

        //Flush the cache in case anything is queued up
        helper.sendAnalyticsCache(NALPEIRON_USERNAME);

        //Cleanup and shutdown library
        helper.cloasNalpLibriry();

        //remove helper from assigned memory
        helper = null;
    }
}
