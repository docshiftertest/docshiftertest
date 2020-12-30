package com.docshifter.core.config.service;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import com.nalpeiron.nalplibrary.NALP;
import com.nalpeiron.nalplibrary.NSA;
import com.nalpeiron.nalplibrary.NSL;
import com.nalpeiron.nalplibrary.NalpError;
import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Profile("licensing")
@Conditional(IsNotInDockerCondition.class)
public class NalpeironService implements ILicensingService {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ILicensingService.class.getName());

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
    @Value("${docshifter.version:DEV")
    private String VERSION;
    @Value("${docshifter.edition:DEV}")
    private String EDITION;
    @Value("${docshifter.build:DEV}")
    private String BUILD;

    //TODO: asses what this does and add sensible data
    private static final String LICENSE_STAT = "???";

    private static NalpeironHelper helper;

    private static final long[] aid = {0L};

    // Advanced settings, for normal operation leave as defaults
    @Value("${nalpeiron.loglevel:6}")
    private int LogLevel; // set log level, please see documentation
    @Value("${nalpeiron.offlinemode:0}")
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
    @Value("${nalpeiron.libdir:./license/}")
    private String libDir;// Workfolder for nalpeiron license and cache files
    @Value("${nalpeiron.workdir:./license/}")
    private String WorkDir;// Workfolder for nalpeiron license and cache files

    @Value("${nalpeiron.offlineactivation:false}")
    private boolean offlineActivation;// will force to do the activation in oofline mode, will forgo all connection attempts to the server

    private final boolean NSAEnable = true; // Enable Analytics
    private final boolean NSLEnable = true; // Enable Licensing

    public static final List<NalpeironHelper.FeatureStatus> VALID_FEATURE_STATUS = Arrays.asList(NalpeironHelper.FeatureStatus.AUTHORIZED);
    public static final List<NalpeironHelper.LicenseStatus> VALID_LICENSE_STATUS = Arrays.asList(NalpeironHelper.LicenseStatus.PROD_AUTHORIZED, NalpeironHelper.LicenseStatus.PROD_INTRIAL, NalpeironHelper.LicenseStatus.PROD_NETWORK, NalpeironHelper.LicenseStatus.PROD_NETWORK_LTCO);

    //TODO: LOGGING
    @PostConstruct
    private void init() {
        logger.info("|===========================| LICENSING SERVICE INIT START |===========================|", null);

        if (!(WorkDir.endsWith("/") || WorkDir.endsWith("\\"))) {
            WorkDir += "/";
        }

        logger.debug("using nalpeiron workdir: " + WorkDir, null);

        //Test if the DLL is present
        NalpeironHelper.dllTest();

        logger.debug("Opening nalpeiron library", null);

        openValidateNalpeironLibrary();

        logger.info("|===========================| LICENSING SERVICE INIT FINISHED |===========================|", null);

    }

    private void openValidateNalpeironLibrary() {
        try {
            //generate a random number between 1 and 500 and use it to calculate the security offset
            int security = 1 + (int) (Math.random() * (501));
            int offset = AUTH_X + ((security * AUTH_Y) % AUTH_Z);

            logger.debug("generated security params for nalpeiron", null);

            //Library open, close and error handling
            NALP nalp = new NALP();

            logger.debug("opened NALP()", null);

            //Analytics functions
            NSA nsa = new NSA(nalp);

            logger.debug("opened NSA()", null);

            //Licensing functions
            NSL nsl = new NSL(nalp, offset);

            logger.debug("opened NSL()", null);

            helper = new NalpeironHelper(nalp, nsa, nsl, WorkDir, offlineActivation);

            logger.debug("initialized NalpeironHelper", null);

            String dllPath = libDir + "/docShifterFileCheck.";
            if (SystemUtils.IS_OS_UNIX) {
                dllPath += "so";
            } else if (SystemUtils.IS_OS_WINDOWS) {
                dllPath += "dll";
            } else {
                int errorCode = 0;//TODO: we need to exit with zero or yajsw will restart the service
                logger.fatal("The operating system you are using is not recognized asn a UNIX or WINDOWS operating system. This is not supported. Stopping Application", null);

                System.exit(errorCode);
            }

            logger.debug("using '" + dllPath + "' as the nalpeiron connection dll", null);

            helper.openNalpLibrary(dllPath, NSAEnable, NSLEnable, LogLevel, WorkDir, LogQLen, CacheQLen, NetThMin,
                    NetThMax, OfflineMode, ProxyIP, ProxyPort, ProxyUsername, ProxyPass, DaemonIP, DaemonPort,
                    DaemonUser, DaemonPass, security);

            //Turn end user privacy off
            helper.setAnalyticsPrivacy(NalpeironHelper.PrivacyValue.OFF.getValue());

            helper.validateLibrary(customerID, productID);

            logger.debug("validateLibrary finished, starting periodic license checking", null);

            helper.validateLicenseAndInitiatePeriodicChecking();

            logger.debug("Periodic license checking thread started, staring analytics", null);

            //At this point we have a license, so start analytics
            //Turn end user privacy off
            helper.setAnalyticsPrivacy(NalpeironHelper.PrivacyValue.OFF.getValue());

            logger.debug("privacy turned off, calling startAnalyticsApp", null);
            helper.startAnalyticsApp(NALPEIRON_USERNAME, CLIENT_DATA, aid);
            logger.debug("sending analytics SystemInfo", null);

            helper.sendAnalyticsSystemInfo(NALPEIRON_USERNAME, APP_LANGUAGE, VERSION, EDITION, BUILD, LICENSE_STAT, CLIENT_DATA);

            helper.sendAnalyticsAndInitiatePeriodicReporting();
            logger.debug("sending analytics SystemInfo, starting periodic analytics sender", null);

            //start periodic sending of analytics
            helper.sendAnalyticsAndInitiatePeriodicReporting();

            logger.debug("Periodic analytics sending thread started", null);
        } catch (DocShifterLicenseException | NalpError e) {
            int errorCode = 0;//TODO: we need to exit with zero or yajsw will restart the service
            logger.fatal("error in docshifter license processing. Could not complete opening and validating Nalpeiron Library.", e);

            System.exit(errorCode);
        }
    }

    public long[] validateAndStartModule(String moduleId, long[] fid) throws DocShifterLicenseException {
        NalpeironHelper.FeatureStatus featureStatus = helper.getFeatureStatus(moduleId);

        if (!VALID_FEATURE_STATUS.contains(featureStatus)) {
            String errorMessage = "feature could not be activated. The feature status is: " + featureStatus.name() + ". Blocking acces to module: " + moduleId;
            DocShifterLicenseException ex = new DocShifterLicenseException(errorMessage);
            logger.info(errorMessage, ex);
            throw ex;
        }

        //At this point we have access to the feature.  do some analytics
        helper.startFeature(NALPEIRON_USERNAME, moduleId, CLIENT_DATA, fid);

        return fid;
    }

    public void endModule(String moduleId, Map<String, Object> clientData, long[] fid) throws DocShifterLicenseException {
        //call end feature
        helper.stopFeature(NALPEIRON_USERNAME, moduleId, clientData, fid);
    }

    @PreDestroy
    private void cleanup() throws Throwable {
        if (helper == null) {
            return;
        }

        //Stop the licenseValidationScheduler
        helper.stopLicenseValidationScheduler();

        //Stop the analyticsSenderScheduler
        helper.stopAnalyticsSenderScheduler();

        //End analytics
        helper.stopAnalyticsApp(NALPEIRON_USERNAME, CLIENT_DATA, aid);

        //Flush the cache in case anything is queued up
        helper.sendAnalyticsCache(NALPEIRON_USERNAME);

        //Cleanup and shutdown library
        helper.closeNalpLibrary();

        //remove helper from assigned memory
        helper = null;
    }
}
