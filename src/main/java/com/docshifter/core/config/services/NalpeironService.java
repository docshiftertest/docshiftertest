package com.docshifter.core.config.services;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
@Service
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
public class NalpeironService implements ILicensingService {

    private static final long[] ANALYTICS_TRANSACTION_ID = {0L};

    @Value("${docshifter.applang:}")
    private String appLanguage;
    @Value("${docshifter.version:DEV")
    private String version;
    @Value("${docshifter.edition:DEV}")
    private String edition;
    @Value("${docshifter.build:DEV}")
    private String build;

    private NalpeironHelper helper;

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

    @Value("${ds.license.code:}")
    private String licenseCode;

    @Value("${nalpeiron.offlineactivation:false}")
    private boolean offlineActivation;// will force to do the activation in offline mode, will forgo all connection
    // attempts to the server

    @Value("${ds.license.activation.request:}")
    private String licenseActivationRequest;

    @Value("${ds.license.activation.answer:}")
    private String licenseActivationAnswer;

    private static final boolean NSAEnable = true; // Enable Analytics
    private static final boolean NSLEnable = true; // Enable Licensing

    private final IContainerChecker containerChecker;

    @Autowired(required = false)
    public NalpeironService(IContainerChecker containerChecker) {
        this.containerChecker = containerChecker;
    }

    public NalpeironService() {
        this(null);
    }

    @PostConstruct
    private void init() {
        log.info("|===========================| LICENSING SERVICE INIT START |===========================|");

        log.debug("Opening nalpeiron library");

        openValidateNalpeironLibrary();

        log.info("|===========================| LICENSING SERVICE INIT FINISHED |===========================|");

    }

    private void openValidateNalpeironLibrary() {
        try {
            //generate a random number between 1 and 500 and use it to calculate the security offset
            int security = 1 + (int) (Math.random() * (501));
            int offset = NalpeironHelper.AUTH_X + ((security * NalpeironHelper.AUTH_Y) % NalpeironHelper.AUTH_Z);

            log.debug("Generated security params for nalpeiron");

            helper = new NalpeironHelper(offset, WorkDir, licenseCode, licenseActivationRequest,
                    licenseActivationAnswer, offlineActivation);

            log.debug("initialized NalpeironHelper");

            if (helper.isPassiveActivation()) {
                helper.openNalpLibrary(LogLevel, LogQLen, security);
            } else {
                helper.openNalpLibrary(NSAEnable, NSLEnable, LogLevel, LogQLen, CacheQLen, NetThMin,
                        NetThMax, OfflineMode, ProxyIP, ProxyPort, ProxyUsername, ProxyPass, DaemonIP, DaemonPort,
                        DaemonUser, DaemonPass, security);

                //Turn end user privacy off
                helper.setAnalyticsPrivacy(NalpeironHelper.PrivacyValue.OFF.getValue());
            }

            helper.validateLibrary(NalpeironHelper.CUSTOMER_ID, NalpeironHelper.PRODUCT_ID);

            log.debug("validateLibrary finished, starting periodic license checking");

            helper.validateLicenseAndInitiatePeriodicChecking();

            doContainerCheck();

            if (!helper.isPassiveActivation()) {
                log.debug("Periodic license checking thread started, staring analytics");

                //At this point we have a license, so start analytics
                //Turn end user privacy off
                helper.setAnalyticsPrivacy(NalpeironHelper.PrivacyValue.OFF.getValue());

                log.debug("privacy turned off, calling startAnalyticsApp");
                helper.startAnalyticsApp(NalpeironHelper.NALPEIRON_USERNAME, NalpeironHelper.CLIENT_DATA, ANALYTICS_TRANSACTION_ID);
                log.debug("sending analytics SystemInfo");

                helper.sendAnalyticsSystemInfo(NalpeironHelper.NALPEIRON_USERNAME, appLanguage, version, edition,
                        build, NalpeironHelper.LICENSE_STAT, NalpeironHelper.CLIENT_DATA);

                helper.sendAnalyticsAndInitiatePeriodicReporting();
                log.debug("sending analytics SystemInfo, starting periodic analytics sender");

                //start periodic sending of analytics
                helper.sendAnalyticsAndInitiatePeriodicReporting();

                log.debug("Periodic analytics sending thread started");
            }
        } catch (Exception e) {
            int errorCode = 0;//TODO: we need to exit with zero or yajsw will restart the service
            log.fatal("Error in DocShifter license processing. Could not complete opening and validating Nalpeiron Library.", e);

            System.exit(errorCode);
        }
    }

    private void doContainerCheck() throws DocShifterLicenseException {
        if (containerChecker == null) {
            return;
        }

        log.debug("Container environment detected.");

        String maxReceiversUDF = helper.getUDFValue(NalpeironHelper.MAX_RECEIVERS_UDF_KEY);
        int maxReceivers = 0;

        if (StringUtils.isNotBlank(maxReceiversUDF)) {
            log.debug("Got maximum allotted receivers: {}", maxReceiversUDF);
            try {
                maxReceivers = Integer.parseInt(maxReceiversUDF);
            } catch (NumberFormatException nfex) {
                log.fatal("Could not parse field value \"" + maxReceiversUDF + "\" as an " +
                        "integer. Please contact DocShifter for support.", nfex);
                System.exit(0);
            }
        }

        try {
            containerChecker.checkReplicas(maxReceivers);
        } catch (Exception ex) {
            log.fatal("Container licensing check failed, exiting application.", ex);
            System.exit(0);
        }

        log.debug("Container licensing check succeeded.");
    }

    public long[] validateAndStartModule(String moduleId, long[] fid) throws DocShifterLicenseException {
        NalpeironHelper.FeatureStatus featureStatus = helper.getFeatureStatus(moduleId);

        if (!featureStatus.isValid()) {
            String errorMessage = "feature could not be activated. The feature status is: " + featureStatus.name() + ". Blocking acces to module: " + moduleId;
            DocShifterLicenseException ex = new DocShifterLicenseException(errorMessage);
            log.info(errorMessage, ex);
            throw ex;
        }

        if (!helper.isPassiveActivation()) {
            //At this point we have access to the feature.  do some analytics
            helper.startFeature(NalpeironHelper.NALPEIRON_USERNAME, moduleId, NalpeironHelper.CLIENT_DATA, fid);
        }

        return fid;
    }

    public void endModule(String moduleId, Map<String, Object> clientData, long[] fid) throws DocShifterLicenseException {
        if (!helper.isPassiveActivation()) {
            //call end feature
            helper.stopFeature(NalpeironHelper.NALPEIRON_USERNAME, moduleId, clientData, fid);
        }
    }

    public boolean hasModuleAccess(String moduleId) throws DocShifterLicenseException {
        return helper.getFeatureStatus(moduleId) == NalpeironHelper.FeatureStatus.AUTHORIZED;
    }

    @PreDestroy
    private void cleanup() throws Throwable {
        if (helper == null) {
            return;
        }

        //Stop the licenseValidationScheduler
        helper.stopLicenseValidationScheduler();

        if (!helper.isPassiveActivation()) {
            //Stop the analyticsSenderScheduler
            helper.stopAnalyticsSenderScheduler();

            //End analytics
            helper.stopAnalyticsApp(NalpeironHelper.NALPEIRON_USERNAME, NalpeironHelper.CLIENT_DATA, ANALYTICS_TRANSACTION_ID);

            //Flush the cache in case anything is queued up
            helper.sendAnalyticsCache(NalpeironHelper.NALPEIRON_USERNAME);
        }

        //Cleanup and shutdown library
        helper.closeNalpLibrary();

        //remove helper from assigned memory
        helper = null;
    }
}
