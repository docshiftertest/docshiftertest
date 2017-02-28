package com.docshifter.core.utils.nalpeiron;

import com.docbyte.utils.Logger;
import com.docshifter.core.exceptions.DocShifterLicenceException;
import com.nalpeiron.nalplibrary.NALP;
import com.nalpeiron.nalplibrary.NSA;
import com.nalpeiron.nalplibrary.NSL;
import com.nalpeiron.nalplibrary.nalpError;
import org.springframework.context.ApplicationContext;

import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NalpeironHelper {

    private final NALP nalp;
    private final NSA nsa;
    private final NSL nsl;

    private ApplicationContext applicationContext;

    private final ScheduledExecutorService licenceValidationScheduler = Executors.newSingleThreadScheduledExecutor();

    public NalpeironHelper(ApplicationContext applicationContext, NALP nalp, NSA nsa, NSL nsl) {

        this.nalp = nalp;
        this.nsa = nsa;
        this.nsl = nsl;
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
        NalpeironLicenseValidator validator = new NalpeironLicenseValidator(this);
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
        SHOWERRORS(0x01, ""),
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
        SHOWERRORS(0x01, ""),
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
        SHOWERRORS(0x01, ""),
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
            throw new IllegalArgumentException("License status not found"); //TODO: wrap in DocShifterLicenceException
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

            throw new IllegalArgumentException("privacy value not found"); //TODO: wrap in DocShifterLicenceException
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

            throw new IllegalArgumentException("privacy value not found"); //TODO: wrap in DocShifterLicenceException
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

            throw new IllegalArgumentException("Activation type not found"); //TODO: wrap in DocShifterLicenceException
        }
    }

    public void openNalpLibriray(String Filename, boolean NSAEnable, boolean NSLEnable, int LogLevel, String WorkDir, int LogQLen, int CacheQLen, int NetThMin, int NetThMax,
                                 int OfflineMode, String ProxyIP, String ProxyPort, String ProxyUsername, String ProxyPass, String DaemonIP, String DaemonPort, String DaemonUser, String DaemonPass, int security) throws DocShifterLicenceException {
        int i = nalp.callNalpLibOpen(Filename, NSAEnable, NSLEnable, LogLevel, WorkDir, LogQLen, CacheQLen, NetThMin, NetThMax, OfflineMode, ProxyIP, ProxyPort, ProxyUsername,
                ProxyPass, DaemonIP, DaemonPort, DaemonUser, DaemonPass, security);

        if (i < 0) {
            throw new DocShifterLicenceException("could not open nalp library", new nalpError(i, nalp.callNalpGetErrorMsg(i)));
        }
    }

    public void cloasNalpLibriry() throws DocShifterLicenceException {
        int i = nalp.callNalpLibClose();

        if (i < 0) {
            throw new DocShifterLicenceException("could not open nalp library", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    public String resolveNalpErrorMsg(int nalpErrorNo) {
        String i = nalp.callNalpGetErrorMsg(nalpErrorNo);
        return i;
    }

    /*public String getLicencingVersion() {
        String i = nsl.callNSLGetVersion();
        return i;
    }*/

    /*public String getComputerID() {
        String i = nsl.callNSLGetComputerID();
        return i;
    }*/

    /*public String getLicenceHostName() {
        String i = nsl.callNSLGetHostName();
        return i;
    }*/

    /*public int getRemainingLeaseSeconds() {
        int i = nsl.callNSLGetLeaseExpSec();
        return i;
    }*/

    /*public String getLeaseExpirationDate() {
        String i = nsl.callNSLGetLeaseExpDate();
        return i;
    }*/

    /*public int getRemainingMaintenanceSeconds() {
        int i = nsl.callNSLGetMaintExpSec();
        return i;
    }*/

    /*public int getRemainingSubscriptionSeconds() {
        int i = nsl.callNSLGetSubExpSec();
        return i;
    }*/

    /*public String getSubscriptionExpirationDate() {
        String i = nsl.callNSLGetSubExpDate();
        return i;
    }*/

    /*public String getMaintenanceExpirationDate() {
        String i = nsl.callNSLGetMaintExpDate();
        return i;
    }*/

    /*public int getRemainingTrialSeconds() {
        int i = nsl.callNSLGetTrialExpSec();
        return i;
    }*/

    /*public String getTrialExpirationDate() {
        String i = nsl.callNSLGetTrialExpDate();
        return i;
    }*/

    public LicenseStatus getLicense(String licenseNo, String xmlRegInfo) {
        int i = nsl.callNSLGetLicense(licenseNo, xmlRegInfo);
        return LicenseStatus.getLicenseStatus(i);
    }

    /*public void returnLicense(String licenseNo) throws DocShifterLicenceException {
        int i = nsl.callNSLReturnLicense(licenseNo);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }*/

    /*public void importCertificate(String licenseNo, String cert) throws DocShifterLicenceException {
        int i = nsl.callNSLImportCertificate(licenseNo, cert);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }*/

    /*public String getActivationCertificateRequest(String licenseNo, String xmlRegInfo) {
        String i = nsl.callNSLGetActivationCertReq(licenseNo, xmlRegInfo);
        return i;
    }*/

    /*public String getDeactivationCertificateRequest(String licenseNo) {
        String i = nsl.callNSLGetDeactivationCertReq(licenseNo);
        return i;
    }*/

    public void validateLibrary(int custID, int prodID) throws DocShifterLicenceException {
        int i = nsl.callNSLValidateLibrary(custID, prodID);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    public LicenseStatus getLicenseStatus() {
        int i = nsl.callNSLGetLicenseStatus();
        return LicenseStatus.getLicenseStatus(i);
    }

    public String getLicenseCode() {
        String i = nsl.callNSLGetLicenseCode();
        return i;
    }

    public LicenceType getLicenseType() {
        int i = nsl.callNSLGetLicenseType();
        return LicenceType.getLicenceType(i);
    }


    public ActivationType getActivationType() {
        int i = nsl.callNSLGetActivationType();
        return ActivationType.getActivationType(i);
    }


    public int getLicenceTimeStamp() throws DocShifterLicenceException {
        int i = nsl.callNSLGetTimeStamp();

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }

        return i;
    }

    public FeatureStatus getFeatureStatus(String featureName) {
        int i = nsl.callNSLGetFeatureStatus(featureName);
        return FeatureStatus.getFeatureStatus(i);
    }

    /*public int checkoutFeature(String featureName, String licCode) {
        int i = nsl.callNSLCheckoutFeature(featureName, licCode);
        return i;
    }*/

    /*public int returnFeature(String featureName, String licenseNo) {
        int i = nsl.callNSLReturnFeature(featureName, licenseNo);
        return i;
    }*/

    /*public int getPoolStatus(String poolName) {
        int i = nsl.callNSLGetPoolStatus(poolName);
        return i;
    }*/

    /*public int checkoutPool(String poolName, String licenseNo, int amt) {
        int i = nsl.callNSLCheckoutPool(poolName, licenseNo, amt);
        return i;
    }*/

    /*public int returnPool(String poolName, String licenseNo, int amt) {
        int i = nsl.callNSLReturnPool(poolName, licenseNo, amt);
        return i;
    }*/

    /*public String getUDFValue(String UDFName) {
        String i = nsl.callNSLGetUDFValue(UDFName);
        return i;
    }*/

    /*public int getNumberAvailableProc(int[] maxProc, int[] availProc) {
        int i = nsl.callNSLGetNumbAvailProc(maxProc, availProc);
        return i;
    }*/

    /*public void registerLicence(String licenseNo, String xmlRegInfo) throws DocShifterLicenceException {
        int i = nsl.callNSLRegister(licenseNo, xmlRegInfo);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }*/

    /*public int testNalpeironLicencingConnection() {
        int i = nsl.callNSLTestConnection();
        return i;
    }*/


    /*public String getAnalyticsVersion() {
        String i = nsa.callNSAGetVersion();
        return i;
    }*/

    /*public void analyticsLogin(String Username, String clientData, long[] lid) throws DocShifterLicenceException {
        int i = nsa.callNSALogin(Username, clientData, lid);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }*/

    public void analyticsLogout(String Username, String clientData, long[] lid) throws DocShifterLicenceException {
        int i = nsa.callNSALogout(Username, clientData, lid);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    /*public String getAnalyticsHostName() {
        String i = nsa.callNSAGetHostName();
        return i;
    }*/

    public void startFeature(String Username, String FeatureCode, String clientData, long[] fid) throws DocShifterLicenceException {
        int i = nsa.callNSAFeatureStart(Username, FeatureCode, clientData, fid);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    public void stopFeature(String Username, String FeatureCode, String clientData, long[] fid) throws DocShifterLicenceException {
        int i = nsa.callNSAFeatureStop(Username, FeatureCode, clientData, fid);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    /*public int ShaferNSAException(String Username, String ExceptionCode, String clientData, String Description) {
        int i = nsa.callNSAException(Username, ExceptionCode, clientData, Description);
        return i;
    }*/

    public void sendAnalyticsSystemInfo(String Username, String Applang, String Version, String Edition, String Build, String LicenseStat, String clientData) throws DocShifterLicenceException {
        int i = nsa.callNSASysInfo(Username, Applang, Version, Edition, Build, LicenseStat, clientData);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    public void sendAnalyticsCache(String Username) throws DocShifterLicenceException {
        int i = nsa.callNSASendCache(Username);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    public void startAnalyticsApp(String username, String clientData, long[] aid) throws DocShifterLicenceException {
        int i = nsa.callNSAApStart(username, clientData, aid);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    public void stopAnalyticsApp(String username, String clientData, long[] aid) throws DocShifterLicenceException {
        int i = nsa.callNSAApStop(username, clientData, aid);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    public void getAnalyticsLocation() throws DocShifterLicenceException {
        int i = nsa.callNSAGetLocation();

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    public void getAnalyticsPrivacy() throws DocShifterLicenceException {
        int i = nsa.callNSAGetPrivacy();

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

    public void setAnalyticsPrivacy(int privacy) throws DocShifterLicenceException {
        int i = nsa.callNSASetPrivacy(privacy);

        if (i < 0) {
            throw new DocShifterLicenceException("Error during licence processing", new nalpError(i, resolveNalpErrorMsg(i)));
        }
    }

   /* public String getAnalyticsStats() {
        String i = nsa.callNSAGetStats();
        return i;
    }*/
}
