package com.docshifter.core.utils.nalpeiron;

import com.docshifter.core.config.service.NalpeironService;
import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nalpeiron.nalplibrary.NALP;
import com.nalpeiron.nalplibrary.NSA;
import com.nalpeiron.nalplibrary.NSL;
import com.nalpeiron.nalplibrary.NalpError;
import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NalpeironHelper {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(NalpeironHelper.class.getName());

    private final int cachingDurationMinutes = 30;
    private final int licenseDurationMinutes = 58;

    private final NALP nalp;
    private final NSA nsa;
    private final NSL nsl;

    private final String workDir;

    private final ScheduledExecutorService licenseValidationScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService analyticsSenderScheduler = Executors.newSingleThreadScheduledExecutor();


    public NalpeironHelper(NALP nalp, NSA nsa, NSL nsl, String workDir) {

        this.nalp = nalp;
        this.nsa = nsa;
        this.nsl = nsl;

        this.workDir = workDir;
    }

    public NALP getNalp() {
        return nalp;
    }

    public NSA getNsa() {
        return nsa;
    }

    public NSL getNsl() {
        return nsl;
    }

    public void stopLicenseValidationScheduler() {
        if (licenseValidationScheduler != null) {
            licenseValidationScheduler.shutdown();
        }
    }

    public void validateLicenseAndInitiatePeriodicChecking() {
        //validate the license and start the periodic checking
        NalpeironLicenseValidator validator = new NalpeironLicenseValidator(this, resolveLicenseNo());
        validator.validateLicenseStatus();
        licenseValidationScheduler.scheduleAtFixedRate(validator, 1, licenseDurationMinutes, TimeUnit.MINUTES);
    }

    public void stopAnalyticsSenderScheduler() {
        if (analyticsSenderScheduler != null) {
            analyticsSenderScheduler.shutdown();
        }
    }

    public void sendAnalyticsAndInitiatePeriodicReporting() {
        NalpeironAnalyticsSender sender = new NalpeironAnalyticsSender(this, NalpeironService.NALPEIRON_USERNAME);
        sender.run();
        analyticsSenderScheduler.scheduleAtFixedRate(sender, 1, cachingDurationMinutes, TimeUnit.MINUTES);
    }

    public static void dllTest() throws DocShifterLicenseException {
        boolean foundNalpLib = false;
        String nalpLibName = "";

        if (SystemUtils.IS_OS_UNIX) {
            nalpLibName = "libnalpjava.so";
        } else if (SystemUtils.IS_OS_WINDOWS) {
            nalpLibName += "nalpjava.dll";
        } else {
            int errorCode = 0;//TODO: we need to exit with zero or yajsw will restart the service
            logger.fatal("The operating system you are using is not recognized asn a UNIX or WINDOWS operating system. This is not supported. Stopping Application", null);
            logger.debug("exited Spring app, doing system.exit()", null);

            System.exit(errorCode);
        }

        String property = System.getProperty("java.library.path");
        StringTokenizer parser = new StringTokenizer(property, ";");
        logger.debug("looking for nalpjava library in the following location", null);

        while (parser.hasMoreTokens()) {
            String libPath = parser.nextToken();
            logger.debug(libPath, null);

            if (libPath.endsWith(nalpLibName)) {
                logger.debug("found " + nalpLibName + " here: " + libPath);
                foundNalpLib = true;
            }

            File pathFile = new File(libPath);
            if (pathFile.isDirectory()) {
                File childPath = new File(libPath + "/" + nalpLibName);
                if (childPath.exists()) {
                    logger.debug("found " + nalpLibName + " here: " + childPath);
                    foundNalpLib = true;
                }
            }

            if (foundNalpLib) {
                return;
            }
        }

        if (!foundNalpLib) {
            logger.fatal("The " + nalpLibName + " file used for yout operating system cammot be found in the included java library paths. Stopping Application", null);
            System.exit(0);
        }
    }


    public enum FeatureStatus {
        //SHOWERRORS(0x01, ""),
        EXPIRED(-5, "Feature request but license expired"),
        UNAUTHORIZED(-4, "Feature not authorized for use"),
        DENIED(-3, "Feature request denied"),
        UNKNOWN(-2, "Unknown Feature requested"),
        ERROR(-1, "Error"),
        UNSET(0, "Unset define to 0 explicitly just in case"),
        AUTHORIZED(1, "Feature authorized for use");

        private final int value;
        private final String message;

        FeatureStatus(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public static FeatureStatus getFeatureStatus(int value) {
            for (FeatureStatus l : FeatureStatus.values()) {
                if (l.value == value) {
                    return l;
                }
            }
            throw new IllegalArgumentException("Feature status not found"); //TODO: wrap in DocShifterLicenseException
        }
    }


    public enum PoolStatus {
        //SHOWERRORS(0x01, ""),
        EXPIRED(-5, "Element request but license expired"),
        UNAUTHORIZED(-4, "Element not authorized for use"),
        DENIED(-3, "Element request denied"),
        UNKNOWN(-2, "Unknown Pool requested"),
        ERROR(-1, "Error"),
        UNSET(0, "Unset"),
        AUTHORIZED(1, "Pool authorized for use");

        private final int value;
        private final String message;

        PoolStatus(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public static PoolStatus getPoolStatus(int value) {
            for (PoolStatus l : PoolStatus.values()) {
                if (l.value == value) {
                    return l;
                }
            }
            throw new IllegalArgumentException("Pool status not found"); //TODO: wrap in DocShifterLicenseException
        }
    }


    public enum LicenseStatus {
        //SHOWERRORS(0x01, ""),
        // [Description("Undetermined")]
        PRODUNDETERMINED(0, ""),
        // [Description("Authorized")]
        PROD_AUTHORIZED(1, ""),
        // [Description("InTrial")]
        PROD_INTRIAL(2, ""),
        // [Description("Product has expired")]
        PROD_CONCURRENT(3, ""),
        // [Description("ActivatedNetwork")]
        PROD_NETWORK(14, ""),
        // [Description("ActivatedNetworkLTCO")]
        PROD_NETWORK_LTCO(15, ""),
        // [Description("ActivatedConcurrent")]
        PROD_PRODEXPIRED(-1, ""),
        // [Description("Backtime Counter Tripped")]
        PROD_BTCOUNTER(-2, ""),
        // [Description("Feature not Authorised")]
        PROD_FEATURESWICHEDOFF(-3, ""),
        // [Description("Feature/Product not Found")]
        PROD_PRODFEATNOTFOUND(-4, ""),
        // [Description("License doesn't verify")]
        PROD_LICENSE_DOESNT_VERIFY(-5, ""),
        // [Description("Returned license to server")]
        PROD_RETURNED_LICENSE(-6, ""),
        // [Description("Date set back too far")]
        PROD_DATE_SET_BACK_TOO_FAR(-7, ""),
        // [Description("Product is InActive")]
        PROD_PRODINACTIVE(-110, ""),
        // [Description("Invalid Trial Period")]
        PROD_INVALIDTRIAL(-111, ""),
        // [Description("ComputerID has already been activated")]
        PROD_COMPUTERIDALREADYACTIVE(-112, ""),
        // [Description("Trial Expired")]
        PROD_EXPIRED(-113, ""),
        // [Description("LicenseCode is inactive")]
        PROD_LCINACTIVE(-114, ""),
        //[Description("Number of Allowed Activations Exceeded")]
        PROD_NOT_AUTHORIZED(-115, ""),
        //[Description("Subscription Expired")]
        PROD_SUBSCRIPTION_EXPIRED(-116, "");

        private final int value;
        private final String message;

        LicenseStatus(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public static LicenseStatus getLicenseStatus(int value) {
            for (LicenseStatus l : LicenseStatus.values()) {
                if (l.value == value) {
                    return l;
                }
            }
            throw new IllegalArgumentException("License status not found");
        }
    }


    public enum PrivacyValue {
        OFF(0, "Privacy is off"),
        ON(1, "privacy is on"),
        NOT_SET(2, "privacy is unset");

        private final int value;
        private final String message;

        PrivacyValue(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public static PrivacyValue getPrivacyValue(int value) {
            for (PrivacyValue p : PrivacyValue.values()) {
                if (p.value == value) {
                    return p;
                }
            }

            throw new IllegalArgumentException("privacy value not found");
        }
    }


    public enum LicenseType {
        UNKNOWN(0, "LicenType unknown"),
        TRIAL(2, "License is trial"),
        PERMANENT(3, "License is permanent"),
        CONCURRENT_PERMANENT(4, "License is permanent and concurrent"),
        SUBSCRIPTION(5, "License is subscription"),
        CONCURRENT_SUBSCRIPTION(6, "License is concurrent subscription"),
        RESERVED(7, "Reserved for internal use");

        private final int value;
        private final String message;

        LicenseType(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public static LicenseType getLicenseType(int value) {
            for (LicenseType p : LicenseType.values()) {
                if (p.value == value) {
                    return p;
                }
            }

            throw new IllegalArgumentException("privacy value not found");
        }
    }

    public enum ActivationType {
        UNKNOWN(0, "unknown"),
        ONLINE(1, "License was activated online"),
        OFFLINE(2, "License was activated with a certificate"),
        DAEMON(3, "License was activated via daemon"),
        RESERVED(4, "Reserved for internal use");

        private final int value;
        private final String message;

        ActivationType(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public static ActivationType getActivationType(int value) {
            for (ActivationType p : ActivationType.values()) {
                if (p.value == value) {
                    return p;
                }
            }

            throw new IllegalArgumentException("Activation type not found");
        }
    }

    public void openNalpLibriray(String Filename, boolean NSAEnable, boolean NSLEnable, int LogLevel, String WorkDir, int LogQLen, int CacheQLen, int NetThMin, int NetThMax,
                                 int OfflineMode, String ProxyIP, String ProxyPort, String ProxyUsername, String ProxyPass, String DaemonIP, String DaemonPort, String DaemonUser, String DaemonPass, int security) throws DocShifterLicenseException {
        try {
            int i = nalp.callNalpLibOpen(Filename, NSAEnable, NSLEnable, LogLevel, WorkDir, LogQLen, CacheQLen, NetThMin, NetThMax, OfflineMode, ProxyIP, ProxyPort, ProxyUsername,
                    ProxyPass, DaemonIP, DaemonPort, DaemonUser, DaemonPass, security);

            if (i < 0) {
                throw new DocShifterLicenseException("could not open nalp library", new NalpError(i, nalp.callNalpGetErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void closeNalpLibriry() throws DocShifterLicenseException {
        try {
            int i = nalp.callNalpLibClose();


            if (i < 0) {
                throw new DocShifterLicenseException("could not open nalp library", new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }

    }

    public String resolveNalpErrorMsg(int nalpErrorNo) throws DocShifterLicenseException {
        try {
            String i = nalp.callNalpGetErrorMsg(nalpErrorNo);
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getLicencingVersion() throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetVersion();
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getComputerID() throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetComputerID();
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getLicenseHostName() throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetHostName();
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public int getRemainingLeaseSeconds() throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetLeaseExpSec();

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetLeaseExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getLeaseExpirationDate() throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetLeaseExpDate();

            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    /*public int getRemainingMaintenanceSeconds() throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetMaintExpSec();
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetMaintExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenseException(error);
        }
    }*/

    public int getRemainingSubscriptionSeconds() throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetSubExpSec();
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetSubExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getSubscriptionExpirationDate() throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetSubExpDate();
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public int getRemainingTrialSeconds() throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetTrialExpSec();
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetTrialExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getTrialExpirationDate() throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetTrialExpDate();
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LicenseStatus getLicense(String licenseNo, String xmlRegInfo) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetLicense(licenseNo, xmlRegInfo);
            return LicenseStatus.getLicenseStatus(i);
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void returnLicense(String licenseNo) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLReturnLicense(licenseNo);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLReturnLicense"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LicenseStatus importCertificate(String licenseNo, String cert) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLImportCertificate(licenseNo, cert);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLImportCertificate"), new NalpError(i, resolveNalpErrorMsg(i)));
            }

            return LicenseStatus.getLicenseStatus(i);
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getActivationCertificateRequest(String licenseNo, String xmlRegInfo) throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetActivationCertReq(licenseNo, xmlRegInfo);
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getDeactivationCertificateRequest(String licenseNo) throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetDeactivationCertReq(licenseNo);
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void validateLibrary(int custID, int prodID) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLValidateLibrary(custID, prodID);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLValidateLibrary"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LicenseStatus getLicenseStatus() throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetLicenseStatus();
            return LicenseStatus.getLicenseStatus(i);
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getLicenseCode() throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetLicenseCode();
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LicenseType getLicenseType() throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetLicenseType();
            return LicenseType.getLicenseType(i);
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }


    public ActivationType getActivationType() throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetActivationType();
            return ActivationType.getActivationType(i);
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }


    public int getLicenseTimeStamp() throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetTimeStamp();

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetTimeStamp"), new NalpError(i, resolveNalpErrorMsg(i)));
            }

            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public FeatureStatus getFeatureStatus(String featureName) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetFeatureStatus(featureName);
            return FeatureStatus.getFeatureStatus(i);
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public FeatureStatus checkoutFeature(String featureName, String licCode) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLCheckoutFeature(featureName, licCode);
            return FeatureStatus.getFeatureStatus(i);
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void returnFeature(String featureName, String licenseNo) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLReturnFeature(featureName, licenseNo);
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLReturnFeature"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public PoolStatus getPoolStatus(String poolName) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetPoolStatus(poolName);
            return PoolStatus.getPoolStatus(i);
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void checkoutPool(String poolName, String licenseNo, int amt) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLCheckoutPool(poolName, licenseNo, amt);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLCheckoutPool"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void returnPool(String poolName, String licenseNo, int amt) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLReturnPool(poolName, licenseNo, amt);
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLReturnPool"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getUDFValue(String UDFName) throws DocShifterLicenseException {
        try {
            String i = nsl.callNSLGetUDFValue(UDFName);
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public int getNumberAvailableSimultaneousLicenses(int[] maxProc, int[] availProc) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLGetNumbAvailProc(maxProc, availProc);
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetNumbAvailProc"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void registerLicense(String licenseNo, String xmlRegInfo) throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLRegister(licenseNo, xmlRegInfo);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLRegister"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void testNalpeironLicencingConnection() throws DocShifterLicenseException {
        try {
            int i = nsl.callNSLTestConnection();
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLTestConnection"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getAnalyticsVersion() throws DocShifterLicenseException {
        try {
            String i = nsa.callNSAGetVersion();
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void analyticsLogin(String Username, String clientData, long[] lid) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSALogin(Username, clientData, lid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSALogin"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void analyticsLogout(String Username, String clientData, long[] lid) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSALogout(Username, clientData, lid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSALogout"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getAnalyticsHostName() throws DocShifterLicenseException {
        try {
            String i = nsa.callNSAGetHostName();
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void startFeature(String Username, String FeatureCode, String clientData, long[] fid) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSAFeatureStart(Username, FeatureCode, clientData, fid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAFeatureStart"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void stopFeature(String Username, String FeatureCode, Map<String, Object> clientData, long[] fid) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSAFeatureStop(Username, FeatureCode, new ObjectMapper().writeValueAsString(clientData), fid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAFeatureStop"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        } catch (JsonProcessingException ex) {
            throw new DocShifterLicenseException(ex);
        }
    }

    public int getAnalyticsExceptionCode(String Username, String ExceptionCode, String clientData, String Description) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSAException(Username, ExceptionCode, clientData, Description);
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void sendAnalyticsSystemInfo(String Username, String Applang, String Version, String Edition, String Build, String LicenseStat, String clientData) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSASysInfo(Username, Applang, Version, Edition, Build, LicenseStat, clientData);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSASysInfo"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void sendAnalyticsCache(String Username) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSASendCache(Username);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSASendCache"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void startAnalyticsApp(String username, String clientData, long[] aid) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSAApStart(username, clientData, aid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAApStart"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void stopAnalyticsApp(String username, String clientData, long[] aid) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSAApStop(username, clientData, aid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAApStop"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void getAnalyticsLocation() throws DocShifterLicenseException {
        try {
            int i = nsa.callNSAGetLocation();

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAGetLocation"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public PrivacyValue getPrivacy() throws DocShifterLicenseException {
        try {
            int i = nsa.callNSAGetPrivacy();
            return PrivacyValue.getPrivacyValue(i);
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void setAnalyticsPrivacy(int privacy) throws DocShifterLicenseException {
        try {
            int i = nsa.callNSASetPrivacy(privacy);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSASetPrivacy"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getAnalyticsStats() throws DocShifterLicenseException {
        try {
            String i = nsa.callNSAGetStats();
            return i;
        } catch (NalpError error) {
            logger.debug("NalpError was thrown in " + error.getStackTrace()[0].getMethodName() + "code=" + error.getErrorCode() + " message=" + error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getLicenseNumber() {
        return nsl.NSLGetLicNo();
    }

    public String resolveLicenseNo() {
        String licenseCode = null;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(workDir + "DSLicenseCode.txt"));
            licenseCode = new String(bytes, Charset.defaultCharset());
        } catch (Exception e) {
            licenseCode = getLicenseCode();
        } finally {
            return licenseCode == null ? "" : licenseCode;
        }
    }

    public String resolveLicenseActivationRequest() {
        String activationRequest = null;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(workDir + "DSLicenseActivationRequest.txt"));
            activationRequest = new String(bytes, Charset.defaultCharset());
        } catch (Exception e) {
            activationRequest = "";
        } finally {
            return activationRequest;
        }
    }

    public String resolveLicenseActivationAnswer() {
        String ActivationAnswer = null;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(workDir + "DSLicenseActivationAnswer.txt"));
            ActivationAnswer = new String(bytes, Charset.defaultCharset());
        } catch (Exception e) {
            ActivationAnswer = "";
        } finally {
            return ActivationAnswer;
        }
    }

    public void writeLicenseActivationRequest(String licenseActivationRequest) throws DocShifterLicenseException {

        try {
            Path outputFilePath = new File(workDir + "DSLicenseActivationRequest.txt").toPath();
            byte[] bytes = licenseActivationRequest.getBytes(Charset.defaultCharset());
            Files.write(outputFilePath, bytes, StandardOpenOption.CREATE);
        } catch (Exception e) {
            throw new DocShifterLicenseException("Could not write the licenseActivationRequest code to file");
        }
    }
}
