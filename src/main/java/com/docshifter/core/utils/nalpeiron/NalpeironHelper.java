package com.docshifter.core.utils.nalpeiron;

import com.docbyte.utils.Logger;
import com.docshifter.core.exceptions.DocShifterLicenceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nalpeiron.nalplibrary.NALP;
import com.nalpeiron.nalplibrary.NSA;
import com.nalpeiron.nalplibrary.NSL;
import com.nalpeiron.nalplibrary.NalpError;
import org.springframework.context.ApplicationContext;

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

    private final NALP nalp;
    private final NSA nsa;
    private final NSL nsl;

    private final String workDir;

    private ApplicationContext applicationContext;

    private final ScheduledExecutorService licenceValidationScheduler = Executors.newSingleThreadScheduledExecutor();

    public NalpeironHelper(ApplicationContext applicationContext, NALP nalp, NSA nsa, NSL nsl, String workDir) {

        this.nalp = nalp;
        this.nsa = nsa;
        this.nsl = nsl;

        this.workDir = workDir;

        this.applicationContext = applicationContext;
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

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void stopLicenceValidationScheduler() {
        if (licenceValidationScheduler != null) {
            licenceValidationScheduler.shutdown();
        }
    }

    public void validateLicenceAndInitiatePeriodicChecking() {
        //validate the licence and start the periodic checking
        NalpeironLicenseValidator validator = new NalpeironLicenseValidator(this, resolveLicenseNo());
        validator.validateLicenceStatus();
        licenceValidationScheduler.scheduleAtFixedRate(validator, 1, 1, TimeUnit.HOURS);
    }

    public static void dllTest() throws DocShifterLicenceException {
        try {
            String property = System.getProperty("java.library.path");
            StringTokenizer parser = new StringTokenizer(property, ";");
            Logger.debug("looking for nalpjava library in the following locations", null);
            while (parser.hasMoreTokens()) {
                Logger.debug(parser.nextToken(), null);
            }
            System.loadLibrary("nalpjava");
        } catch (java.lang.UnsatisfiedLinkError e) {
            Logger.debug("Could not load the nalpjava library", e);
            throw new DocShifterLicenceException(e);
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
            throw new IllegalArgumentException("Feature status not found"); //TODO: wrap in DocShifterLicenceException
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
            throw new IllegalArgumentException("Pool status not found"); //TODO: wrap in DocShifterLicenceException
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


    public enum LicenceType {
        UNKNOWN(0, "LicenType unknown"),
        TRIAL(2, "License is trial"),
        PERMANENT(3, "License is permanent"),
        CONCURRENT_PERMANENT(4, "License is permanent and concurrent"),
        SUBSCRIPTION(5, "License is subscription"),
        CONCURRENT_SUBSCRIPTION(6, "License is concurrent subscription"),
        RESERVED(7, "Reserved for internal use");

        private final int value;
        private final String message;

        LicenceType(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public static LicenceType getLicenceType(int value) {
            for (LicenceType p : LicenceType.values()) {
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
                                 int OfflineMode, String ProxyIP, String ProxyPort, String ProxyUsername, String ProxyPass, String DaemonIP, String DaemonPort, String DaemonUser, String DaemonPass, int security) throws DocShifterLicenceException {
        try {
            int i = nalp.callNalpLibOpen(Filename, NSAEnable, NSLEnable, LogLevel, WorkDir, LogQLen, CacheQLen, NetThMin, NetThMax, OfflineMode, ProxyIP, ProxyPort, ProxyUsername,
                    ProxyPass, DaemonIP, DaemonPort, DaemonUser, DaemonPass, security);

            if (i < 0) {
                throw new DocShifterLicenceException("could not open nalp library", new NalpError(i, nalp.callNalpGetErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void closeNalpLibriry() throws DocShifterLicenceException {
        try {
            int i = nalp.callNalpLibClose();


            if (i < 0) {
                throw new DocShifterLicenceException("could not open nalp library", new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }

    }

    public String resolveNalpErrorMsg(int nalpErrorNo) throws DocShifterLicenceException {
        try {
            String i = nalp.callNalpGetErrorMsg(nalpErrorNo);
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getLicencingVersion() throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetVersion();
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getComputerID() throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetComputerID();
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getLicenceHostName() throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetHostName();
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public int getRemainingLeaseSeconds() throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetLeaseExpSec();

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetLeaseExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getLeaseExpirationDate() throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetLeaseExpDate();

            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    /*public int getRemainingMaintenanceSeconds() throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetMaintExpSec();
            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetMaintExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }*/

    public int getRemainingSubscriptionSeconds() throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetSubExpSec();
            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetSubExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getSubscriptionExpirationDate() throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetSubExpDate();
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public int getRemainingTrialSeconds() throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetTrialExpSec();
            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetTrialExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getTrialExpirationDate() throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetTrialExpDate();
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public LicenseStatus getLicense(String licenseNo, String xmlRegInfo) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetLicense(licenseNo, xmlRegInfo);
            return LicenseStatus.getLicenseStatus(i);
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void returnLicense(String licenseNo) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLReturnLicense(licenseNo);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLReturnLicense"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public LicenseStatus importCertificate(String licenseNo, String cert) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLImportCertificate(licenseNo, cert);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLImportCertificate"), new NalpError(i, resolveNalpErrorMsg(i)));
            }

            return LicenseStatus.getLicenseStatus(i);
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getActivationCertificateRequest(String licenseNo, String xmlRegInfo) throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetActivationCertReq(licenseNo, xmlRegInfo);
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getDeactivationCertificateRequest(String licenseNo) throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetDeactivationCertReq(licenseNo);
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void validateLibrary(int custID, int prodID) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLValidateLibrary(custID, prodID);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLValidateLibrary"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public LicenseStatus getLicenseStatus() throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetLicenseStatus();
            return LicenseStatus.getLicenseStatus(i);
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getLicenseCode() throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetLicenseCode();
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public LicenceType getLicenseType() throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetLicenseType();
            return LicenceType.getLicenceType(i);
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }


    public ActivationType getActivationType() throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetActivationType();
            return ActivationType.getActivationType(i);
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }


    public int getLicenceTimeStamp() throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetTimeStamp();

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetTimeStamp"), new NalpError(i, resolveNalpErrorMsg(i)));
            }

            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public FeatureStatus getFeatureStatus(String featureName) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetFeatureStatus(featureName);
            return FeatureStatus.getFeatureStatus(i);
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public FeatureStatus checkoutFeature(String featureName, String licCode) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLCheckoutFeature(featureName, licCode);
            return FeatureStatus.getFeatureStatus(i);
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void returnFeature(String featureName, String licenseNo) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLReturnFeature(featureName, licenseNo);
            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLReturnFeature"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public PoolStatus getPoolStatus(String poolName) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetPoolStatus(poolName);
            return PoolStatus.getPoolStatus(i);
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void checkoutPool(String poolName, String licenseNo, int amt) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLCheckoutPool(poolName, licenseNo, amt);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLCheckoutPool"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void returnPool(String poolName, String licenseNo, int amt) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLReturnPool(poolName, licenseNo, amt);
            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLReturnPool"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getUDFValue(String UDFName) throws DocShifterLicenceException {
        try {
            String i = nsl.callNSLGetUDFValue(UDFName);
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public int getNumberAvailableSimultaneousLicences(int[] maxProc, int[] availProc) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLGetNumbAvailProc(maxProc, availProc);
            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetNumbAvailProc"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void registerLicence(String licenseNo, String xmlRegInfo) throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLRegister(licenseNo, xmlRegInfo);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLRegister"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void testNalpeironLicencingConnection() throws DocShifterLicenceException {
        try {
            int i = nsl.callNSLTestConnection();
            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLTestConnection"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getAnalyticsVersion() throws DocShifterLicenceException {
        try {
            String i = nsa.callNSAGetVersion();
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void analyticsLogin(String Username, String clientData, long[] lid) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSALogin(Username, clientData, lid);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSALogin"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void analyticsLogout(String Username, String clientData, long[] lid) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSALogout(Username, clientData, lid);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSALogout"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getAnalyticsHostName() throws DocShifterLicenceException {
        try {
            String i = nsa.callNSAGetHostName();
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void startFeature(String Username, String FeatureCode, String clientData, long[] fid) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSAFeatureStart(Username, FeatureCode, clientData, fid);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAFeatureStart"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void stopFeature(String Username, String FeatureCode, Map<String, Object> clientData, long[] fid) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSAFeatureStop(Username, FeatureCode, new ObjectMapper().writeValueAsString(clientData), fid);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAFeatureStop"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError | JsonProcessingException error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public int getAnalyticsExceptionCode(String Username, String ExceptionCode, String clientData, String Description) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSAException(Username, ExceptionCode, clientData, Description);
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void sendAnalyticsSystemInfo(String Username, String Applang, String Version, String Edition, String Build, String LicenseStat, String clientData) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSASysInfo(Username, Applang, Version, Edition, Build, LicenseStat, clientData);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSASysInfo"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void sendAnalyticsCache(String Username) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSASendCache(Username);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSASendCache"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void startAnalyticsApp(String username, String clientData, long[] aid) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSAApStart(username, clientData, aid);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAApStart"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void stopAnalyticsApp(String username, String clientData, long[] aid) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSAApStop(username, clientData, aid);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAApStop"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void getAnalyticsLocation() throws DocShifterLicenceException {
        try {
            int i = nsa.callNSAGetLocation();

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAGetLocation"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public PrivacyValue getPrivacy() throws DocShifterLicenceException {
        try {
            int i = nsa.callNSAGetPrivacy();
            return PrivacyValue.getPrivacyValue(i);
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public void setAnalyticsPrivacy(int privacy) throws DocShifterLicenceException {
        try {
            int i = nsa.callNSASetPrivacy(privacy);

            if (i < 0) {
                throw new DocShifterLicenceException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSASetPrivacy"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getAnalyticsStats() throws DocShifterLicenceException {
        try {
            String i = nsa.callNSAGetStats();
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenceException(error);
        }
    }

    public String getLicenseNumber() {
        return nsl.NSLGetLicNo();
    }

    public String resolveLicenseNo() {
        String licenseCode = null;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get( workDir +"DSLicenseCode.txt"));
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
            byte[] bytes = Files.readAllBytes(Paths.get( workDir +"DSLicenseActivationRequest.txt"));
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
            byte[] bytes = Files.readAllBytes(Paths.get( workDir + "DSLicenseActivationAnswer.txt"));
            ActivationAnswer = new String(bytes, Charset.defaultCharset());
        } catch (Exception e) {
            ActivationAnswer = "";
        } finally {
            return ActivationAnswer;
        }
    }

    public void writeLicenseActivationRequest(String licenseActivationRequest) throws DocShifterLicenceException {

        try {
            Path outputFilePath = new File(workDir + "DSLicenseActivationRequest.txt").toPath();
            byte[] bytes = licenseActivationRequest.getBytes(Charset.defaultCharset());
            Files.write(outputFilePath, bytes, StandardOpenOption.CREATE);
        } catch (Exception e) {
            throw new DocShifterLicenceException("Could not write the licenseActivationRequest code to file");
        }
    }
}
