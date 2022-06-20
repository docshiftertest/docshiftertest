package com.docshifter.core.config.services;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.licensing.dtos.LicensingDto;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
@Service
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
public class NalpeironService implements ILicensingService {

    private static final long[] ANALYTICS_TRANSACTION_ID = {0L};
    private static final Path persistentLicDirPath = Paths.get("/opt/DocShifter/data/licensing");
    private static final Path persistentLicPath = persistentLicDirPath.resolve(System.getenv("HOSTNAME"));

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
    private final WebClient licensingApiClient;

    @Autowired(required = false)
    public NalpeironService(@Qualifier("licensingApiClient") WebClient licensingApiClient,
                            IContainerChecker containerChecker) {
        this.licensingApiClient = licensingApiClient;
        this.containerChecker = containerChecker;
    }

    public NalpeironService(@Qualifier("licensingApiClient") WebClient licensingApiClient) {
        this(licensingApiClient, null);
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
            log.fatal("Error in DocShifter license processing, exiting application.", e);

            System.exit(errorCode);
        }
    }

    /**
     * Fetches the maxReceivers Total Application Agility (TAA) field on the license and uses a containerChecker (if
     * any available for the environment) to make sure the number of active receivers doesn't exceed this value.
     * @throws DocShifterLicenseException Something went wrong while fetching the value of the TAA field or while
     * performing the check.
     */
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
                throw new DocShifterLicenseException("Could not parse field value \"" + maxReceiversUDF + "\" as an " +
                        "integer. Please contact DocShifter for support.", nfex);
            }
        }

        Set<String> replicas = containerChecker.checkReplicas(maxReceivers);

        if (Files.exists(persistentLicDirPath)) {
            try (Stream<Path> stream = Files.walk(persistentLicDirPath)) {
                final Map<String, String> licenseCodeMap = Collections.singletonMap("licenseCode", helper.getLicenseCode());
                Flux.fromStream(stream)
                        // Current replica is already excluded by the checkReplicas method so no need to take that scenario
                        // into account (otherwise we'd have to exclude/ignore it manually from the replicas set as
                        // it's not normal that a file corresponding to the current instance already exists)
                        .filter(path -> !replicas.contains(path.getFileName().toString()))
                        // Need to switch scheduler here because Files.readAllBytes is a blocking I/O call and could
                        // cause thread starvation else
                        // For the truly dedicated: you could look into changing this to truly async I/O
                        .publishOn(Schedulers.boundedElastic())
                        .map(path -> {
                                    try {
                                        return new AbstractMap.SimpleImmutableEntry<>(path.getFileName().toString(),
                                                new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                                    } catch (IOException ioe) {
                                        throw new RuntimeException(ioe);
                                    }
                        }).onErrorContinue((ex, path) -> log.warn("Exception occurred while trying to read {}, a " +
                                "licensing error might occur later down the line!", path, ex)).onErrorStop()
                        // And back to the parallel scheduler, so we can send our async requests to the licensing API...
                        .publishOn(Schedulers.parallel())
                        .map(entry -> {
                            Map<String, String> uriVariables = new HashMap<>(licenseCodeMap);
                            if (StringUtils.isEmpty(entry.getValue())) {
                                uriVariables.put("hostname", entry.getKey());
                            } else {
                                uriVariables.put("computerId", entry.getValue());
                            }
                            return uriVariables;
                        })
                        .flatMap(uriVariables -> licensingApiClient.delete()
                                .uri("/activation", uriVariables)
                                .retrieve()
                                .bodyToMono(LicensingDto.class)
                        ).onErrorContinue((ex, entry) -> log.warn("Exception while sending request to licensing API, " +
                                "a licensing error might occur later down the line!", ex)).onErrorStop()
                        .blockLast();
            } catch (IOException ex) {
                throw new DocShifterLicenseException("Could not walk through " + persistentLicDirPath, ex);
            }
        } else {
            try {
                Files.createDirectories(persistentLicDirPath);
            } catch (IOException ioe) {
                throw new DocShifterLicenseException("Unable to create" + persistentLicDirPath, ioe);
            }
        }

        try {
            Files.write(persistentLicPath, helper.getComputerID().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ioe) {
            throw new DocShifterLicenseException("Could not create persistent licensing file: " + persistentLicPath, ioe);
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

            // Need to return license in a container environment because each instance counts as a new activation
            if (containerChecker != null) {
                helper.returnLicense(licenseCode);
                Files.deleteIfExists(persistentLicPath);
            }
        }

        //Cleanup and shutdown library
        helper.closeNalpLibrary();

        //remove helper from assigned memory
        helper = null;
    }
}
