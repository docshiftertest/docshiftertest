package com.docshifter.core.config.service;

import com.docbyte.utils.Logger;
import com.docshifter.core.exceptions.DocShifterLicenceException;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import com.nalpeiron.nalplibrary.NALP;
import com.nalpeiron.nalplibrary.NSA;
import com.nalpeiron.nalplibrary.NSL;
import com.nalpeiron.nalplibrary.NalpError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class NalpeironService {

    //These private ints are unique to your product and must
    // be set here to the values corresponding to your product.
    private static final int customerID = 4863;
    private static final int productID = 100; // last 5 digits of 6561300100
    private static final int AUTH_X = 375; // N{5...499}
    private static final int AUTH_Y = 648; // N{501...999}
    private static final int AUTH_Z = 263; // N{233...499}
    private static final String CLIENT_DATA = "";

    //TODO: fill in some sensible values
    public static final String NALPEIRON_USERNAME = "";

    @Value("${docshifter.applang:}")
    private String APP_LANGUAGE;
    @Value("${build.version:DEV")
    private String VERSION;
    @Value("${docshifter.edition:DEV}")
    private String EDITION;
    @Value("${build.version:DEV}")
    private String BUILD;

    //TODO: asses what this does and add sensible data
    private static final String LICENCE_STAT = "???";

    private static NalpeironHelper helper;

    private static final long[] aid = {0L};

    private ApplicationContext applicationContext;

    // Advanced settings, for normal operation leave as defaults
    @Value("${nalpeiron.loglevel:6}")
    private int LogLevel; // set log level, please see documentation
    @Value("${nalpeiron.oflinemode:0}")
    private int OfflineMode; // Select Offline mode, please see documentation, only for analytics, licensing will always access internet when available
    @Value("${nalpeiron.maxlogqueue:300}")
    private int LogQLen; // please see documentation
    @Value("${nalpeiron.maxchachequeue:35}")
    private int CacheQLen; // please see documentation
    @Value("${nalpeiron.networkminthreads:25}")
    private int NetThMin; // please see documentation
    @Value("${nalpeiron.networkmaxthreads:25}")
    private int NetThMax; // please see documentation
    @Value("${nalpeiron.proxyip:}")
    private String ProxyIP; // InternetConnection Proxy IP Address if required
    @Value("${nalpeiron.proxyport:}")
    private String ProxyPort; // InternetConnection Proxy Port if required
    @Value("${nalpeiron.proxyusername:}")
    private String ProxyUsername; // InternetConnection Proxy Username if required
    @Value("${nalpeiron.proxypassword:}")
    private String ProxyPass; // InternetConnection Proxy Password if required
    @Value("${nalpeiron.daemonip:}")
    private String DaemonIP; //Daemon IP
    @Value("${nalpeiron.daemonport:}")
    private String DaemonPort; //Daemon Port
    @Value("${nalpeiron.daemonuser:}")
    private String DaemonUser; //Daemon User
    @Value("${nalpeiron.daemonpassword:}")
    private String DaemonPass; //Daemon Password
    @Value("${nalpeiron.workdir:./license/}")
    private String WorkDir;// Workfolder for nalpeiron license and cache files
    private final boolean NSAEnable = true; // Enable Analytics
    private final boolean NSLEnable = true; // Enable Licensing

    public static final List<NalpeironHelper.FeatureStatus> VALID_FEATURE_STATUS = Arrays.asList(NalpeironHelper.FeatureStatus.AUTHORIZED);
    public static final List<NalpeironHelper.LicenseStatus> VALID_LICENCE_STATUS = Arrays.asList(NalpeironHelper.LicenseStatus.PROD_AUTHORIZED, NalpeironHelper.LicenseStatus.PROD_INTRIAL, NalpeironHelper.LicenseStatus.PROD_NETWORK, NalpeironHelper.LicenseStatus.PROD_NETWORK_LTCO);

    @Autowired
    public NalpeironService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    //TODO: LOGGING
    @PostConstruct
    private void init() {
        try {
            //Test if the DLL is present
            NalpeironHelper.dllTest();
        } catch (DocShifterLicenceException e) {
            Logger.fatal("nalpjava library could not be found, or the manifest could not be read", e);
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

            helper = new NalpeironHelper(applicationContext, nalp, nsa, nsl, WorkDir);

            helper.openNalpLibriray(WorkDir + "docShifterFileCheck.dll", NSAEnable, NSLEnable, LogLevel, WorkDir, LogQLen, CacheQLen, NetThMin, NetThMax, OfflineMode, ProxyIP, ProxyPort, ProxyUsername, ProxyPass, DaemonIP, DaemonPort, DaemonUser, DaemonPass, security);

            //Turn end user privacy off
            helper.setAnalyticsPrivacy(NalpeironHelper.PrivacyValue.OFF.getValue());

            helper.validateLibrary(customerID, productID);

            helper.validateLicenceAndInitiatePeriodicChecking();

            //At this point we have a license, so start analytics
            helper.startAnalyticsApp(NALPEIRON_USERNAME, CLIENT_DATA, aid);
            helper.sendAnalyticsSystemInfo(NALPEIRON_USERNAME, APP_LANGUAGE, VERSION,
                    EDITION, BUILD, LICENCE_STAT, CLIENT_DATA);

        } catch (DocShifterLicenceException | NalpError e) {
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
        helper.startFeature(NALPEIRON_USERNAME, moduleId, "<firstname>Joe</firstname><lastname>Bloggs</lastname><field1>field1data</field1>", fid);

        return fid;
    }

    public void endModule(String moduleId, Map<String, Object> clientData, long[] fid) throws DocShifterLicenceException {
        //call end feature
        helper.stopFeature(NALPEIRON_USERNAME, moduleId, clientData, fid);
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
        helper.closeNalpLibriry();

        //remove helper from assigned memory
        helper = null;
    }
}
