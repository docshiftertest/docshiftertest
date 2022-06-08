package com.docshifter.core.utils.nalpeiron;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nalpeiron.nalplibrary.NALP;
import com.nalpeiron.nalplibrary.NSA;
import com.nalpeiron.nalplibrary.NSL;
import com.nalpeiron.NalpError;
import com.nalpeiron.passlibrary.PSL;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * To update the JNI bindings of the Nalpeiron library from the samples they provide (e.g. for the passive classes):
 * <ol>
 * <li>Make sure Linux .so variant is prefixed with "lib", otherwise the {@code System.loadLibrary} call will fail!</li>
 * <li>Copy the sample classes to the {@code com.nalpeiron.passlibrary} package (except the {@code NalpError} class
 * which we customized the most)</li>
 * <li>Replace all occurrences of <i>complete word</i> {@code nalpError} with {@code NalpError} in the sample
 * classes</li>
 * <li>Fix import of {@code NalpError} and constructor calls to it. E.g. {@code throw new NalpError(i, nalp
 * .callNalpGetErrorMsg(i), "quiet");} should just become throw new {@code NalpError(i, nalp.callNalpGetErrorMsg(i));}
 * as we got rid of that former constructor signature. To accomplish this you can do a global replace of {@code ,"quiet"}
 * with an empty string.</li>
 * <li>Add {@code @Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)} to classes and replace all occurrences of
 * {@code System.out.println} with {@code log.error}</li>
 * <li>Add the following static code block to the {@code NALP} class, which will load in the appropriate native library:
 * <pre>
 * // Open the JNI wrapper library. Use static initialization block
 * // so that we only do this once no matter how many NALPs are created
 * static {
 *     try {
 * 	       System.loadLibrary("PassiveFilechck");
 *     } catch (Exception ex) {
 *         log.warn("Tried to load PassiveFilechck native lib but it failed. Subsequent JNI calls will fail. Is it " +
 *             "inaccessible?", ex);
 *     }
 * }</pre></li>
 * <li>Make sure this class still compiles (e.g. if Nalpeiron has modified/deprecated/removed some bindings make sure
 * to reflect it in here)</li>
 * </ol>
 */
@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
public class NalpeironHelper {
    /**
     * Format of the datetime Strings the Nalpeiron libs return. E.g.: Wed Jul 17 19:59:12 2013
     */
    private static final DateTimeFormatter NALP_DATE_FORMAT = DateTimeFormatter.ofPattern("E MMM dd hh:mm:ss y",
            new Locale("en", "US"));
    public static final String LICENSING_IDENTIFIER = "licensing";
    public static final String EXPIRY_DATE_UDF_KEY = "pslExpiryDate";
    public static final String MAX_RECEIVERS_UDF_KEY = "maxReceivers";

    //These private ints are unique to your product and must
    // be set here to the values corresponding to your product.
    public static final int CUSTOMER_ID = 4863;
    public static final int PRODUCT_ID = 100; // last 5 digits of 6561300100
    public static final int AUTH_X = 375; // N{5...499}
    public static final int AUTH_Y = 648; // N{501...999}
    public static final int AUTH_Z = 263; // N{233...499}
    public static final String CLIENT_DATA = "";

    //TODO: fill in some sensible values
    public static final String NALPEIRON_USERNAME = "";
    //TODO: assess what this does and add sensible data
    public static final String LICENSE_STAT = "???";
    private static final int CACHING_DURATION_MINUTES = 30;
    private static final int LICENSE_DURATION_MINUTES = 58;

    private final com.nalpeiron.passlibrary.NALP nalpPassive;
    private final PSL psl;
    private final NALP nalp;
    private final NSA nsa;
    private final NSL nsl;

    private final Path workDir;
    private final String workDirStr;

    private final String licenseCode;
    private final String licenseActivationRequest;
    private final String licenseActivationAnswer;
    private final boolean offlineActivation;

    private final ScheduledExecutorService licenseValidationScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService analyticsSenderScheduler = Executors.newSingleThreadScheduledExecutor();


    public NalpeironHelper(int offset, String workDir, String licenseCode,
                           String activationRequest, String activationAnswer, boolean offlineActivation) {
        this.workDirStr = workDir.endsWith("/") || workDir.endsWith("\\") ? workDir : workDir + File.separatorChar;
        log.debug("Using nalpeiron workdir: {}", workDirStr);
        this.workDir = Paths.get(workDirStr);
        this.licenseCode = resolveLicenseNo(licenseCode);
        this.licenseActivationRequest = resolveLicenseActivationRequest(activationRequest);
        this.licenseActivationAnswer = resolveLicenseActivationAnswer(activationAnswer);

        // Library open, close and error handling
        // If we have a license code and a license activation answer, but it was provided without a matching activation
        // request, we're likely dealing with a passive license here
        if (StringUtils.isNotBlank(this.licenseCode)
                && StringUtils.isBlank(this.licenseActivationRequest)
                && StringUtils.isNotBlank(this.licenseActivationAnswer)) {
            this.offlineActivation = true;

            nalpPassive = new com.nalpeiron.passlibrary.NALP();

            log.debug("Opened NALPPassive()");

            psl = new PSL(nalpPassive, offset);

            log.debug("Opened PSL()");

            nalp = null;
            nsa = null;
            nsl = null;
        } else {
            this.offlineActivation = offlineActivation;

            nalp = new NALP();

            log.debug("opened NALP()");

            //Analytics functions
            nsa = new NSA(nalp);

            log.debug("opened NSA()");

            //Licensing functions
            nsl = new NSL(nalp, offset);

            log.debug("opened NSL()");

            nalpPassive = null;
            psl = null;
        }
    }

    public boolean isPassiveActivation() {
        return nalpPassive != null;
    }

    public void stopLicenseValidationScheduler() {
        if (licenseValidationScheduler != null) {
            licenseValidationScheduler.shutdown();
        }
    }

    public void validateLicenseAndInitiatePeriodicChecking() {
        //validate the license and start the periodic checking
        NalpeironLicenseValidator validator = new NalpeironLicenseValidator(this);
        validator.validateLicenseStatus();
        licenseValidationScheduler.scheduleAtFixedRate(validator, 1, LICENSE_DURATION_MINUTES, TimeUnit.MINUTES);
    }

    public void stopAnalyticsSenderScheduler() {
        if (analyticsSenderScheduler != null) {
            analyticsSenderScheduler.shutdown();
        }
    }

    public void sendAnalyticsAndInitiatePeriodicReporting() {
        NalpeironAnalyticsSender sender = new NalpeironAnalyticsSender(this, NALPEIRON_USERNAME);
        sender.run();
        analyticsSenderScheduler.scheduleAtFixedRate(sender, 1, CACHING_DURATION_MINUTES, TimeUnit.MINUTES);
    }


    public enum FeatureStatus {
        //SHOWERRORS(0x01, ""),
        EXPIRED(-5, "Feature request but license expired"),
        UNAUTHORIZED(-4, "Feature not authorized for use"),
        DENIED(-3, "Feature request denied"),
        UNKNOWN(-2, "Unknown Feature requested"),
        ERROR(-1, "Error"),
        UNSET(0, "Unset define to 0 explicitly just in case"),
        AUTHORIZED(1, "Feature authorized for use", true);

        private final int value;
        private final String message;
        private final boolean valid;

        FeatureStatus(int value, String message) {
            this(value, message, false);
        }

        FeatureStatus(int value, String message, boolean valid) {
            this.value = value;
            this.message = message;
            this.valid = valid;
        }

        public int getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isValid() {
            return valid;
        }

        public static FeatureStatus getFeatureStatus(int value) throws DocShifterLicenseException {
            for (FeatureStatus l : FeatureStatus.values()) {
                if (l.value == value) {
                    return l;
                }
            }
            throw new DocShifterLicenseException("Feature status not found");
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

        public static PoolStatus getPoolStatus(int value) throws DocShifterLicenseException {
            for (PoolStatus l : PoolStatus.values()) {
                if (l.value == value) {
                    return l;
                }
            }
            throw new DocShifterLicenseException("Pool status not found");
        }
    }


    public enum LicenseStatus {
        //SHOWERRORS(0x01, ""),
        // [Description("Undetermined")]
        PRODUNDETERMINED(0, ""),
        // [Description("Authorized")]
        PROD_AUTHORIZED(1, "", true),
        // [Description("InTrial")]
        PROD_INTRIAL(2, "", true),
        // [Description("Product has expired")]
        PROD_CONCURRENT(3, ""),
        // [Description("ActivatedNetwork")]
        PROD_NETWORK(14, "", true),
        // [Description("ActivatedNetworkLTCO")]
        PROD_NETWORK_LTCO(15, "", true),
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
        private final boolean valid;

        LicenseStatus(int value, String message) {
            this(value, message, false);
        }

        LicenseStatus(int value, String message, boolean valid) {
            this.value = value;
            this.message = message;
            this.valid = valid;
        }

        public int getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }

        public boolean isValid() {
            return valid;
        }

        public static LicenseStatus getLicenseStatus(int value) throws DocShifterLicenseException {
            for (LicenseStatus l : LicenseStatus.values()) {
                if (l.value == value) {
                    return l;
                }
            }
            throw new DocShifterLicenseException("License status not found");
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

        public static PrivacyValue getPrivacyValue(int value) throws DocShifterLicenseException {
            for (PrivacyValue p : PrivacyValue.values()) {
                if (p.value == value) {
                    return p;
                }
            }

            throw new DocShifterLicenseException("privacy value not found");
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

        public static LicenseType getLicenseType(int value) throws DocShifterLicenseException {
            for (LicenseType p : LicenseType.values()) {
                if (p.value == value) {
                    return p;
                }
            }

            throw new DocShifterLicenseException("privacy value not found");
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

        public static ActivationType getActivationType(int value) throws DocShifterLicenseException{
            for (ActivationType p : ActivationType.values()) {
                if (p.value == value) {
                    return p;
                }
            }

            throw new DocShifterLicenseException("Activation type not found");
        }
    }

    public void openNalpLibrary(boolean NSAEnable, boolean NSLEnable, int LogLevel,
                                int LogQLen, int CacheQLen, int NetThMin, int NetThMax, int OfflineMode,
                                String ProxyIP, String ProxyPort, String ProxyUsername, String ProxyPass,
                                String DaemonIP, String DaemonPort, String DaemonUser, String DaemonPass, int security)
            throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new IllegalStateException("Wrong method call (called active variant, but this is a passive " +
                    "activation!)");
        }
        try {
            int i = nalp.callNalpLibOpen(NSAEnable, NSLEnable, LogLevel, workDirStr, "", LogQLen, CacheQLen, NetThMin,
                    NetThMax, OfflineMode, ProxyIP, ProxyPort, ProxyUsername,
                    ProxyPass, DaemonIP, DaemonPort, DaemonUser, DaemonPass, security);

            if (i < 0) {
                throw new DocShifterLicenseException("could not open nalp library" + nalp.callNalpGetErrorMsg(i), new NalpError(i, nalp.callNalpGetErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void openNalpLibrary(int LogLevel, int LogQLen, int security) throws DocShifterLicenseException {
        if (!isPassiveActivation()) {
            throw new IllegalStateException("Wrong method call (called passive variant, but this is an active " +
                    "activation!)");
        }
        try {
            int i = nalpPassive.callPSLLibOpen(LogLevel, licenseCode, workDirStr, LogQLen, security);

            if (i < 0) {
                throw new DocShifterLicenseException("could not open nalp passive library" + nalpPassive.callPSLGetErrorMsg(i),
                        new NalpError(i, nalpPassive.callPSLGetErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void closeNalpLibrary() throws DocShifterLicenseException {
        try {
            int i;
            if (isPassiveActivation()) {
                i = nalpPassive.callPSLLibClose();
            } else {
                i = nalp.callNalpLibClose();
            }

            if (i < 0) {
                throw new DocShifterLicenseException("could not close nalp library", new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }

    }

    public String resolveNalpErrorMsg(int nalpErrorNo) throws DocShifterLicenseException {
        try {
            if (isPassiveActivation()) {
                return nalpPassive.callPSLGetErrorMsg(nalpErrorNo);
            }
            return nalp.callNalpGetErrorMsg(nalpErrorNo);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getLicencingVersion() throws DocShifterLicenseException {
        try {
            if (isPassiveActivation()) {
                return psl.callPSLGetVersion();
            }
            return nsl.callNSLGetVersion();
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getComputerID() throws DocShifterLicenseException {
        try {
            if (isPassiveActivation()) {
                return psl.callPSLGetComputerID();
            }
            return nsl.callNSLGetComputerID();
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getLicenseHostName() throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getLicenseHostName() is not supported in passive lib");
        }
        try {
            return nsl.callNSLGetHostName();
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public int getRemainingLeaseSeconds() throws DocShifterLicenseException {
        try {
            int i;
            if (isPassiveActivation()) {
                i = psl.callPSLGetLeaseExpSec();
            } else {
                i = nsl.callNSLGetLeaseExpSec();
            }

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetLeaseExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LocalDateTime getLeaseExpirationDate() throws DocShifterLicenseException {
        try {
            String dateString;
            if (isPassiveActivation()) {
                dateString = psl.callPSLGetLeaseExpDate();
            } else {
                dateString = nsl.callNSLGetLeaseExpDate();
            }
            return LocalDateTime.parse(dateString, NALP_DATE_FORMAT);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public int getRemainingMaintenanceSeconds() throws DocShifterLicenseException {
        try {
            int i;
            if (isPassiveActivation()) {
                i = psl.callPSLGetMaintExpSec();
            } else {
                i = nsl.callNSLGetMaintExpSec();
            }
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetMaintExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            throw new DocShifterLicenseException(error);
        }
    }

    public LocalDateTime getMaintenanceExpirationDate() throws DocShifterLicenseException {
        try {
            String dateString;
            if (isPassiveActivation()) {
                dateString = psl.callPSLGetMaintExpDate();
            } else {
                dateString = nsl.callNSLGetMaintExpDate();
            }
            return LocalDateTime.parse(dateString, NALP_DATE_FORMAT);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public long getRemainingSubscriptionSeconds() throws DocShifterLicenseException {
        try {
            if (isPassiveActivation()) {
                // Simulate this functionality in the passive library by using an Application Agility field
                LocalDateTime expDate = getSubscriptionExpirationDate();
                if (LocalDateTime.MAX.equals(expDate)) {
                    return Long.MAX_VALUE;
                }
                return Math.max(0, ChronoUnit.SECONDS.between(LocalDateTime.now(), expDate));
            }
            int i = nsl.callNSLGetSubExpSec();
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetSubExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LocalDateTime getSubscriptionExpirationDate() throws DocShifterLicenseException {
        try {
            if (isPassiveActivation()) {
                // Simulate this functionality in the passive library by using an Application Agility field
                String dateString = psl.callPSLGetUDFValue(EXPIRY_DATE_UDF_KEY);
                if (StringUtils.isBlank(dateString)) {
                    return LocalDateTime.MAX;
                }
                if (StringUtils.containsIgnoreCase(dateString, "T")) {
                    return LocalDateTime.parse(dateString);
                } else {
                    return LocalDate.parse(dateString).atTime(LocalTime.MAX);
                }
            }
            return LocalDateTime.parse(nsl.callNSLGetSubExpDate(), NALP_DATE_FORMAT);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        } catch (DateTimeParseException ex) {
            throw new DocShifterLicenseException("Could not parse date string: " + ex.getParsedString(), ex);
        }
    }

    public int getRemainingTrialSeconds() throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getRemainingTrialSeconds() is not supported in passive lib");
        }
        try {
            int i = nsl.callNSLGetTrialExpSec();
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetTrialExpSec"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LocalDateTime getTrialExpirationDate() throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getTrialExpirationDate() is not supported in passive lib");
        }
        try {
            return LocalDateTime.parse(nsl.callNSLGetTrialExpDate(), NALP_DATE_FORMAT);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LicenseStatus getLicense(String licenseNo, String xmlRegInfo) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getLicense(String, String) is not supported in passive lib");
        }
        try {
            int i = nsl.callNSLObtainLicense(licenseNo, xmlRegInfo, "");
            return LicenseStatus.getLicenseStatus(i);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void returnLicense(String licenseNo) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("returnLicense(String) is not supported in passive lib");
        }
        try {
            int i = nsl.callNSLReturnLicense(licenseNo);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLReturnLicense"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LicenseStatus importCertificate(String licenseNo, String cert) throws DocShifterLicenseException {
        try {
            int i;
            if (isPassiveActivation()) {
                i = psl.callPSLImportLicense(licenseNo, cert);
            } else {
                i = nsl.callNSLImportCertificate(licenseNo, cert);
            }
            return postProcessLicenseStatus(i);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getActivationCertificateRequest(String licenseNo, String xmlRegInfo, String specialID) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getActivationCertificateRequest(String, String, String) is not " +
                    "supported in passive lib");
        }
        try {
            return nsl.callNSLRequestActivationCert(licenseNo, xmlRegInfo, specialID);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getDeactivationCertificateRequest(String licenseNo) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getDeactivationCertificateRequest(String) is not supported in " +
                    "passive lib");
        }
        try {
            return nsl.callNSLGetDeactivationCertReq(licenseNo);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void validateLibrary(int custID, int prodID) throws DocShifterLicenseException {
        try {
            int i;
            if (isPassiveActivation()) {
                i = psl.callPSLValidateLibrary(custID, prodID);
            } else {
                i = nsl.callNSLValidateLibrary(custID, prodID);
            }

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLValidateLibrary"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public LicenseStatus getLicenseStatus() throws DocShifterLicenseException {
        try {
            int i;
            if (isPassiveActivation()) {
                i = psl.callPSLGetLicenseStatus();
            } else {
                i = nsl.callNSLGetLicenseStatus();
            }
            return postProcessLicenseStatus(i);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    /**
     *
     * @param i
     * @return
     * @throws DocShifterLicenseException
     */
    private LicenseStatus postProcessLicenseStatus(int i) throws DocShifterLicenseException {
        LicenseStatus licStatus = LicenseStatus.getLicenseStatus(i);

        if (!isPassiveActivation() || !licStatus.isValid()) {
            return licStatus;
        }

        // Simulate expiration functionality in the passive library by using an Application Agility field
        long remaining = getRemainingSubscriptionSeconds();
        if (remaining <= 0) {
            return LicenseStatus.PROD_EXPIRED;
        }
        return licStatus;
    }

    private String getLicenseCodeInternal() throws DocShifterLicenseException {
        try {
            if (isPassiveActivation()) {
                return psl.callPSLGetLicenseCode();
            }
            return nsl.callNSLGetLicenseCode();
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public LicenseType getLicenseType() throws DocShifterLicenseException {
        try {
            if (isPassiveActivation()) {
                // Simulate this functionality in the passive library by using an Application Agility field
                if (LocalDateTime.MAX.equals(getSubscriptionExpirationDate())) {
                    return LicenseType.PERMANENT;
                }
                return LicenseType.SUBSCRIPTION;
            }
            int i = nsl.callNSLGetLicenseType();
            return LicenseType.getLicenseType(i);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }


    public ActivationType getActivationType() throws DocShifterLicenseException {
        try {
            if (isPassiveActivation()) {
                return ActivationType.OFFLINE;
            }
            int i = nsl.callNSLGetActivationType();
            return ActivationType.getActivationType(i);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }


    public int getLicenseTimeStamp() throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getLicenseTimeStamp() is not supported in passive lib");
        }
        try {
            int i = nsl.callNSLGetTimeStamp();

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetTimeStamp"), new NalpError(i, resolveNalpErrorMsg(i)));
            }

            return i;
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public FeatureStatus getFeatureStatus(String featureName) throws DocShifterLicenseException {
        try {
            int i;
            if (isPassiveActivation()) {
                i = psl.callPSLGetFeatureStatus(featureName);
            } else {
                i = nsl.callNSLGetFeatureStatus(featureName);
            }
            FeatureStatus featStatus = FeatureStatus.getFeatureStatus(i);
            if (!isPassiveActivation() || !featStatus.isValid()) {
                return featStatus;
            }

            // Simulate expiration functionality in the passive library by using an Application Agility field
            long remaining = getRemainingSubscriptionSeconds();
            if (remaining <= 0) {
                return FeatureStatus.EXPIRED;
            }
            return featStatus;
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public FeatureStatus checkoutFeature(String featureName, String licCode) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("checkoutFeature(String, String) is not supported in passive lib");
        }
        try {
            int i = nsl.callNSLCheckoutFeature(featureName, licCode);
            return FeatureStatus.getFeatureStatus(i);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void returnFeature(String featureName, String licenseNo) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("returnFeature(String, String) is not supported in passive lib");
        }
        try {
            int i = nsl.callNSLReturnFeature(featureName, licenseNo);
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLReturnFeature"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public PoolStatus getPoolStatus(String poolName) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getPoolStatus(String) is not supported in passive lib");
        }
        try {
            int i = nsl.callNSLGetPoolInfo(poolName);
            return PoolStatus.getPoolStatus(i);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void checkoutPool(String poolName, String licenseNo, int amt) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("checkoutPool(String, String, int) is not supported in passive " +
                    "lib");
        }
        try {
            int i = nsl.callNSLCheckoutPool(poolName, licenseNo, amt);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLCheckoutPool"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void returnPool(String poolName, String licenseNo, int amt) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("returnPool(String, String, int) is not supported in passive lib");
        }
        try {
            int i = nsl.callNSLReturnPool(poolName, licenseNo, amt);
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLReturnPool"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getUDFValue(String UDFName) throws DocShifterLicenseException {
        try {
            if (isPassiveActivation()) {
                return psl.callPSLGetUDFValue(UDFName);
            }
            return nsl.callNSLGetUDFValue(UDFName);
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public int getNumberAvailableSimultaneousLicenses() throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getNumberAvailableSimultaneousLicenses() is not supported in " +
                    "passive lib");
        }
        try {
            int i = nsl.callNSLGetAvailProcs();
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLGetNumbAvailProc"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
            return i;
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void registerLicense(String licenseNo, String xmlRegInfo) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("registerLicense(String, String) is not supported in passive lib");
        }
        try {
            int i = nsl.callNSLRegister(licenseNo, xmlRegInfo);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLRegister"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void testNalpeironLicencingConnection(long connTO, long transTO) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("testNalpeironLicencingConnection(long, long) is not supported in" +
                    " passive lib");
        }
        try {
            int i = nsl.callNSLTestConnection2(connTO, transTO);
            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSLTestConnection"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getAnalyticsVersion() throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getAnalyticsVersion() is not supported in passive lib");
        }
        try {
            String i = nsa.callNSAGetVersion();
            return i;
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void analyticsLogin(String Username, String clientData, long[] lid) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("analyticsLogin(String, String long[]) is not supported in " +
                    "passive lib");
        }
        try {
            int i = nsa.callNSALogin(Username, clientData, lid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSALogin"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void analyticsLogout(String Username, String clientData, long[] lid) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("analyticsLogout(String, String, long[]) is not supported in " +
                    "passive lib");
        }
        try {
            int i = nsa.callNSALogout(Username, clientData, lid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSALogout"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getAnalyticsHostName() throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getAnalyticsHostName() is not supported in passive lib");
        }
        try {
            return nsa.callNSAGetHostName();
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void startFeature(String Username, String FeatureCode, String clientData, long[] fid) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("startFeature(String, String, String, long[]) is not supported in" +
                    " passive lib");
        }
        try {
            int i = nsa.callNSAFeatureStart(Username, FeatureCode, clientData, fid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAFeatureStart"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void stopFeature(String Username, String FeatureCode, Map<String, Object> clientData, long[] fid) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("stopFeature(String, String, Map<String, Object>, long[]) is not" +
                    " supported in passive lib");
        }
        try {
            int i = nsa.callNSAFeatureStop(Username, FeatureCode, new ObjectMapper().writeValueAsString(clientData), fid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAFeatureStop"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        } catch (JsonProcessingException ex) {
            throw new DocShifterLicenseException(ex);
        }
    }

    public int getAnalyticsExceptionCode(String Username, String ExceptionCode, String clientData, String Description) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getAnalyticsExceptionCode(String, String, String, String) is not" +
                    " supported in passive lib");
        }
        try {
            int i = nsa.callNSAException(Username, ExceptionCode, clientData, Description);
            return i;
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void sendAnalyticsSystemInfo(String Username, String Applang, String Version, String Edition, String Build, String LicenseStat, String clientData) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("sendAnalyticsSystemInfo(String, String, String, String, String, " +
                    "String, String) is not supported in passive lib");
        }
        try {
            int i = nsa.callNSASysInfo(Username, Applang, Version, Edition, Build, LicenseStat, clientData);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSASysInfo"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
            log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void sendAnalyticsCache(String Username) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("sendAnalyticsCache(String) is not supported in passive lib");
        }
        try {
            int i = nsa.callNSASendCache(Username);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSASendCache"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
                        log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void startAnalyticsApp(String username, String clientData, long[] aid) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getAnalyticsStats() is not supported in passive lib");
        }
        try {
            int i = nsa.callNSAApStart(username, clientData, aid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAApStart"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
                        log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void stopAnalyticsApp(String username, String clientData, long[] aid) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("stopAnalyticsApp(String, String, long[]) is not supported in " +
                    "passive lib");
        }
        try {
            int i = nsa.callNSAApStop(username, clientData, aid);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSAApStop"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
                        log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public PrivacyValue getPrivacy() throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getPrivacy() is not supported in passive lib");
        }
        try {
            int i = nsa.callNSAGetPrivacy();
            return PrivacyValue.getPrivacyValue(i);
        } catch (NalpError error) {
                        log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public void setAnalyticsPrivacy(int privacy) throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("setAnalyticsPrivacy(int) is not supported in passive lib");
        }
        try {
            int i = nsa.callNSASetPrivacy(privacy);

            if (i < 0) {
                throw new DocShifterLicenseException(String.format("Error in Nalpeiron library: failed to execute %s.", "callNSASetPrivacy"), new NalpError(i, resolveNalpErrorMsg(i)));
            }
        } catch (NalpError error) {
                        log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public String getAnalyticsStats() throws DocShifterLicenseException {
        if (isPassiveActivation()) {
            throw new UnsupportedOperationException("getAnalyticsStats() is not supported in passive lib");
        }
        try {
            String i = nsa.callNSAGetStats();
            return i;
        } catch (NalpError error) {
                        log.debug("NalpError was thrown in {} code={} message={}", error.getStackTrace()[0].getMethodName(),
                    error.getErrorCode(), error.getErrorMessage(), error);
            throw new DocShifterLicenseException(error);
        }
    }

    public boolean isOfflineActivation() {
        return offlineActivation;
    }

    private String resolveLicenseNo(String licenseCode) {
        if (StringUtils.isBlank(licenseCode)) {
            log.debug("DS_LICENSE_CODE environment variable is not set or empty, will try to read DSLicenseCode.txt.");
            Path licenseCodePath = workDir.resolve("DSLicenseCode.txt");
            try {
                byte[] bytes = Files.readAllBytes(licenseCodePath);
                licenseCode = new String(bytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("Error while reading DSLicenseCode.txt! Will try resolving license code internally.", e);
                try {
                    licenseCode = getLicenseCodeInternal();
                } catch (Exception e2) {
                    log.error("And got an error while trying to resolve the license code internally!", e2);
                }
            }
        }
        log.debug("Final license code is {}", licenseCode);
        return licenseCode == null ? "" : licenseCode.trim();
    }

    private String resolveLicenseActivationRequest(String activationRequest) {
        if (StringUtils.isNotBlank(activationRequest)) {
            log.debug("Final license activation request is {}", activationRequest);
            return activationRequest;
        }

        log.debug("DS_LICENSE_ACTIVATION_REQUEST environment variable is not set or empty, will try to read" +
                "DSLicenseActivationRequest.txt.");
        Path activationRequestPath = workDir.resolve("DSLicenseActivationRequest.txt");
        try {
            if (!Files.exists(activationRequestPath)) {
                log.debug("...But {} does not exist! Returning empty string.", activationRequestPath);
                return "";
            }
            byte[] bytes = Files.readAllBytes(activationRequestPath);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error while reading DSLicenseActivationRequest.txt!", e);
            return "";
        }
    }

    public String getLicenseActivationRequest() {
        return licenseActivationRequest;
    }

    private String resolveLicenseActivationAnswer(String activationAnswer) {
        if (StringUtils.isNotBlank(activationAnswer)) {
            log.debug("Final license activation answer is {}", activationAnswer);
            return activationAnswer;
        }

        log.debug("DS_LICENSE_ACTIVATION_ANSWER environment variable is not set or empty, will try to read" +
                "DSLicenseActivationAnswer.txt.");
        Path activationAnswerPath = workDir.resolve("DSLicenseActivationAnswer.txt");
        try {
            if (!Files.exists(activationAnswerPath)) {
                log.debug("...But {} does not exist! Returning empty string.", activationAnswerPath);
                return "";
            }
            byte[] bytes = Files.readAllBytes(activationAnswerPath);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error while reading DSLicenseActivationAnswer.txt!", e);
            return "";
        }
    }

    public String getLicenseActivationAnswer() {
        return licenseActivationAnswer;
    }

    public void writeLicenseActivationRequest(String licenseActivationRequest) throws DocShifterLicenseException {
        try {
            Path outputFilePath = workDir.resolve("DSLicenseActivationRequest.txt");
            byte[] bytes = licenseActivationRequest.getBytes(StandardCharsets.UTF_8);
            Files.write(outputFilePath, bytes, StandardOpenOption.CREATE);
        } catch (Exception e) {
            throw new DocShifterLicenseException("Could not write the licenseActivationRequest code to file", e);
        }
    }
}
