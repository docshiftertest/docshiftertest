package com.docshifter.core.config.services;

import com.docshifter.core.config.entities.Module;
import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.licensing.dtos.LicensingDto;
import com.docshifter.core.utils.FileUtils;
import com.docshifter.core.utils.NetworkUtils;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
@Service
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
public class NalpeironService implements ILicensingService {
    private static final String LICENSE_MANAGEMENT_API_KEY = "3626307e-3592-4180-b9df-1b5ceb663e90";
    private static final long[] ANALYTICS_TRANSACTION_ID = {0L};
    /**
     * The directory pointing to the persistent licensing files managed by DocShifter itself for the purposes of
     * ghost activation cleanup (after a previous instance crashed). Only used in a containerized environment.
     */
    private static final Path persistentLicDirPath = Paths.get("/opt/DocShifter/data/licensing");
    /**
     * The current license file (named after the hostname of the instance) in the persistent directory, which is managed
     * by DocShifter itself for the purposes of ghost activation cleanup (after a previous instance crashed). Only
     * used in a containerized environment.
     */
    private static final Path persistentLicPath = persistentLicDirPath.resolve(NetworkUtils.getLocalHostName());

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

    /**
     * An instance that can be used to find other components in the cluster. Should be autowired in any container
     * environment and for every component.
     */
    private final IContainerClusterer containerClusterer;
    /**
     * An instance that can be used to verify no license constraints are being violated in a container cluster.
     * Typically, we only set restrictions on the total number of Receivers a customer may run in their cluster (for
     * now), so this instance should be autowired in any container environment but only when we're running the Receiver
     * component.
     */
    private final IContainerChecker containerChecker;
    private final WebClient licensingApiClient;

    public NalpeironService(@Qualifier("licensingApiClient") WebClient licensingApiClient,
                            @Nullable IContainerClusterer containerClusterer,
                            @Nullable IContainerChecker containerChecker) {
        this.licensingApiClient = licensingApiClient;
        this.containerClusterer = containerClusterer;
        this.containerChecker = containerChecker;
    }

    @PostConstruct
    private void init() {
        log.info("|===========================| LICENSING SERVICE INIT START |===========================|");

        try {
            log.debug("Opening nalpeiron library");
            openValidateNalpeironLibrary();

            if (containerClusterer != null) {
                log.debug("Container environment detected.");
                if (!helper.isPassiveActivation()) {
                    log.debug("Checking for previous container crashes");
                    checkLicenseCleanup();
                }
            }

            validateLicenseAndStartAnalytics();

            doContainerCheck();
        } catch (Exception ex) {
            log.fatal("Error in DocShifter license processing, exiting application.", ex);
            // We need to exit with zero or yajsw will restart the service
            System.exit(0);
        }

        log.info("|===========================| LICENSING SERVICE INIT FINISHED |===========================|");

    }

    /**
     * Checks if any previous container instances have exited abnormally, therefore requiring a license cleanup. It
     * does this by keeping track of some persistent files in {@link #persistentLicDirPath}, the names of the files
     * will refer to the hostnames/container names of all instances that are/were running, the content of the files
     * will refer to the computerIDs of the instances as provided by Nalpeiron. On normal application shutdown, these
     * files will normally be cleaned up and the license will be returned properly. However, after an application
     * crash, the license will not have been returned as usual. So whenever another licensed instance starts up,
     * there will be a "ghost activation" remaining. To prevent this, we check which licensed components are
     * currently still running on the cluster and compare this to the list of files in the
     * {@link #persistentLicDirPath}. If there are any extraneous files present there, that means we found such a
     * ghost activation, and thus we can make a call to the DocShifter License Management API (Lambda) to clear up the
     * activation for the offending hostname/computerID.
     * @throws DocShifterLicenseException If the path pointing to our persistent licensing files is somehow
     * inaccessible, or we could not manage any of the persistent licensing files or create a new one. It's safer to
     * just crash and burn than to continue with a potentially inconsistent licensing state in that case...
     */
    private void checkLicenseCleanup() throws DocShifterLicenseException {
        if (Files.exists(persistentLicDirPath)) {
            try (Stream<Path> stream = Files.walk(persistentLicDirPath)) {
                final String licenseCode = helper.getLicenseCode();
                Flux.fromStream(stream)
                        // Current replica is already excluded by the listOtherReplicas method so no need to take that
                        // scenario into account (otherwise we'd have to exclude/ignore it manually from the replicas
                        // set as it's not normal that a file corresponding to the current instance already exists)
                        // Run this on the main thread as it's a blocking operation using the cluster API, and we
                        // preferably don't want to run multiple calls at once as the results are most likely
                        // being cached so subsequent calls should go a lot faster.
                        .publishOn(Schedulers.immediate())
                        .filter(Files::isRegularFile)
                        .filter(path -> {
                            String fileName = path.getFileName().toString();
                            Set<String> replicas;
                            try {
                                replicas = containerClusterer.listOtherReplicas(fileName);
                            } catch (DocShifterLicenseException ex) {
                                log.warn("Exception occurred while trying to list all replicas for component backed " +
                                        "by instance file {}, a licensing error might occur later down the line!",
                                        fileName, ex);
                                return false;
                            }
                            return !replicas.contains(fileName);
                        })
                        // Need to switch scheduler here because Files.readAllBytes is a blocking I/O call and could
                        // cause thread starvation else
                        // TODO: For the truly dedicated: you could look into changing this to truly async I/O
                        .publishOn(Schedulers.boundedElastic())
                        .flatMapIterable(path -> {
                            String[] computerIds;
                            try (Stream<String> lineStream = Files.lines(path)) {
                                computerIds = lineStream.filter(StringUtils::isNotEmpty).toArray(String[]::new);
                            } catch (IOException ioe) {
                                log.warn("Failed to read contents of {}, will try to use hostname instead.", path);
                                computerIds = null;
                            }

                            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
                            queryParams.add("licenseCode", licenseCode);
                            queryParams.add("hostname", path.getFileName().toString());
                            if (computerIds == null || computerIds.length == 0) {
                                return List.of(new AbstractMap.SimpleImmutableEntry<>(path, queryParams));
                            }

                            return Arrays.stream(computerIds).map(computerId -> {
                                queryParams.add("computerId", computerId);
                                return new AbstractMap.SimpleImmutableEntry<>(path, queryParams);
                            }).toList();
                        }).onErrorContinue((ex, path) -> log.warn("Exception occurred while trying to read {}, a " +
                                "licensing error might occur later down the line!", path, ex)).onErrorStop()
                        // And back to the parallel scheduler, so we can send our async requests to the licensing API...
                        .publishOn(Schedulers.parallel())
                        .flatMap(entry -> licensingApiClient.delete()
                                .uri(uriBuilder -> uriBuilder
                                        .path("/activation")
                                        .queryParams(entry.getValue())
                                        .build())
                                .header("x-api-key", LICENSE_MANAGEMENT_API_KEY)
                                .retrieve()
                                // Treat HTTP 404 as success if no mention was made about the licenseCode because
                                // that means the API then returned that because no matching hostname/computerID was
                                // found on Nalpeiron. In that case we want to continue and clean up the file to
                                // prevent it from being processed again at startup by a different instance.
                                .onStatus(HttpStatus.NOT_FOUND::equals,
                                        resp -> resp.bodyToMono(LicensingDto.class)
                                                // If there happens to be no body present in the response, continue
                                                // with an empty LicensingDto instead of aborting the chain with an
                                                // empty Mono
                                                .defaultIfEmpty(new LicensingDto())
                                                .filter(dto -> {
                                                    if (dto.getMessage() != null && dto.getMessage().contains("licenseCode")) {
                                                        return true;
                                                    }
                                                    // In our query params: we either sent the hostname or the
                                                    // computerId to identify the activation we want to delete...
                                                    // Assume by default we sent the computerId
                                                    String queryKeySent = "computerId";
                                                    String queryValueSent = entry.getValue().getFirst(queryKeySent);
                                                    // But if the value matching the computerId in the query params map
                                                    // is null, i.e. we didn't pass it to the API, then we must have
                                                    // sent the hostname through instead, so log that one instead then
                                                    if (queryValueSent == null) {
                                                        queryKeySent = "hostname";
                                                        queryValueSent = entry.getValue().getFirst(queryKeySent);
                                                    }
                                                    log.warn("Trying to delete an activation for {} with value {} " +
                                                                    "returned an HTTP 404 error! Has the activation " +
                                                                    "already been cleared? Will delete persistent " +
                                                                    "licensing file at {}, so we won't retry this " +
                                                                    "in the future.",
                                                            queryKeySent, queryValueSent, entry.getKey());
                                                    return false;
                                                })
                                                .flatMap(dto -> resp.createException()))
                                .bodyToMono(LicensingDto.class)
                                .defaultIfEmpty(new LicensingDto())
                                .map(dto -> entry)
                        ).onErrorContinue((ex, entry) -> log.warn(
                                "Exception while sending request to licensing API, a licensing error might occur " +
                                        "later down the line!", ex)).onErrorStop()
                        // And finally delete the file so other instances don't pick it up anymore, do that on the
                        // boundedElastic scheduler again as it's a blocking I/O operation
                        .publishOn(Schedulers.boundedElastic())
                        .map(entry -> {
                            try {
                                // If we used the hostname, simply delete the entire file, otherwise delete the line
                                // matching the computerId in the file (or just the entire file if it was the only
                                // entry in there)
                                if (entry.getValue().containsKey("hostname")) {
                                    Files.deleteIfExists(entry.getKey());
                                } else {
                                    FileUtils.deleteLineOrFileIfEmpty(entry.getKey(), entry.getValue().getFirst("computerId"));
                                }
                            } catch (IOException ioe) {
                                log.warn("Exception while trying to delete licensing file {}, perhaps it has been " +
                                        "deleted already?", entry.getKey(), ioe);
                            }
                            return entry;
                        })
                        .blockLast();
            } catch (IOException ioe) {
                throw new DocShifterLicenseException("Could not walk through " + persistentLicDirPath + " but it " +
                        "seems to exist, is it somehow a file instead of a directory?", ioe);
            }
        } else {
            try {
                Files.createDirectories(persistentLicDirPath);
            } catch (IOException ioe) {
                throw new DocShifterLicenseException("Unable to create" + persistentLicDirPath, ioe);
            }
        }

        try {
            persistComputerId(helper.getComputerID());
        } catch (IOException ioe) {
            throw new DocShifterLicenseException("Could not create or write to persistent licensing file: " + persistentLicPath, ioe);
        }
    }

    /**
     * Saves a computerId entry to the persistent licensing path.
     * @param computerId The computerId to save.
     * @throws IOException Something went wrong while trying to write to the persistent licensing file.
     */
    private void persistComputerId(String computerId) throws IOException {
        Files.writeString(persistentLicPath, computerId + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
    }

    /**
     * Validates that the computerId of the current machine hasn't changed since last time it was checked and cached
     * into memory. If a change has occurred, then this will also replace the previously persisted computerId with its
     * new value.
     * @return {@code true} if the computerId has changed since last time and the new one has been persisted, {@code false}
     * if nothing in particular has happened or if an error occurred while replacing the computerId in the persistent
     * file.
     */
    private boolean checkComputerId() {
        try {
            String lastComputerId = helper.getCachedComputerId();
            String computerId = helper.getComputerID();
            if (!computerId.equals(lastComputerId)) {
                log.debug("The computerId has seemingly changed since last time! Will update {} to {} in {} so it can" +
                        " be cleaned on next startup", lastComputerId, computerId, persistentLicPath);
                persistComputerId(computerId);
                FileUtils.deleteLineOrFileIfEmpty(persistentLicPath, lastComputerId);
                return true;
            }
        } catch (Exception ex) {
            log.error("Unable to check or persist computerId to {}", persistentLicPath, ex);
        }
        return false;
    }

    /**
     * Opens the Nalpeiron library and checks if it hasn't been tampered with.
     * @throws DocShifterLicenseException Could not properly open or validate the Nalpeiron library, so you should
     * probably kill the application...
     */
    private void openValidateNalpeironLibrary() throws DocShifterLicenseException {
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

        log.debug("validateLibrary finished");
    }

    /**
     * Attempts to load in the license, checks if it is valid, fires off a recurring license checker and starts the
     * entire analytics ceremony.
     * @throws DocShifterLicenseException Potentially something horrible went wrong during some licensing check, so you
     * should probably kill the application...
     */
    private void validateLicenseAndStartAnalytics() throws DocShifterLicenseException {
        // Make sure to verify our persisted computer ID in containers after checking into the Nalpeiron servers
        // because the actual computer ID tends to change each time we do that
        Runnable postCheckAction = null;
        if (containerClusterer != null && !helper.isPassiveActivation()) {
            postCheckAction = this::checkComputerId;
        }

        log.debug("Starting periodic license checking");
        helper.validateLicenseAndInitiatePeriodicChecking(postCheckAction);
        log.debug("Periodic license checking thread started");

        if (helper.isPassiveActivation()) {
            return;
        }

        log.debug("Starting analytics");

        //At this point we have a license, so start analytics
        //Turn end user privacy off
        helper.setAnalyticsPrivacy(NalpeironHelper.PrivacyValue.OFF.getValue());

        log.debug("privacy turned off, calling startAnalyticsApp");
        helper.startAnalyticsApp(NalpeironHelper.NALPEIRON_USERNAME, NalpeironHelper.CLIENT_DATA, ANALYTICS_TRANSACTION_ID);
        log.debug("sending analytics SystemInfo");

        helper.sendAnalyticsSystemInfo(NalpeironHelper.NALPEIRON_USERNAME, appLanguage, version, edition,
                build, NalpeironHelper.LICENSE_STAT, NalpeironHelper.CLIENT_DATA);

        log.debug("sending analytics SystemInfo, starting periodic analytics sender");

        // Start periodic sending of analytics
        helper.sendAnalyticsAndInitiatePeriodicReporting(postCheckAction);

        log.debug("Periodic analytics sending thread started");
    }

    /**
     * Fetches the maxReceivers Total Application Agility (TAA) field on the license and uses a containerChecker (if
     * any available for the environment) to make sure the number of active receivers doesn't exceed this value.
     * @throws DocShifterLicenseException Something went wrong while fetching the value of the TAA field or while
     * performing the check. You should probably kill the application if this happens...
     */
    private void doContainerCheck() throws DocShifterLicenseException {
        String maxReceiversUDF = helper.getUDFValue(NalpeironHelper.MAX_RECEIVERS_UDF_KEY);
        if (containerClusterer == null && StringUtils.isNotBlank(maxReceiversUDF)) {
            throw new DocShifterLicenseException("Your license code appears to be suited for a containerized " +
                    "installation but no container environment was detected. Please contact DocShifter for support.");
        }
        if (containerChecker == null) {
            return;
        }
        log.debug("Performing container check");
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

        containerChecker.performCheck(maxReceivers);
    }

    /**
     * Attempts to start a module and registers its usage with the analytics server.
     * @param moduleId The module/feature to start.
     * @param fid Identifier for the transaction. The same one should be passed whenever you call
     * {@link #endModule(String, Map, long[])} later on. The array should only contain a single element. If this
     * element is set to 0 then the Nalpeiron server will swap it with a random value. If {@code null} is passed, the
     * transactions will not be grouped.
     * @return The {@code fid} array that you passed and that the Nalpeiron server optionally transformed.
     * @throws DocShifterLicenseException Could not start the module, as it's very likely not licensed.
     * Alternatively, something went wrong while recording analytics.
     */
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

    /**
     * Attempts to record an end of the module usage with the analytics server.
     * @param moduleId The module/feature to end.
     * @param clientData a string containing a valid XML fragment with whatever data you care to pass to the Nalpeiron
     * server or daemon with your call.
     * @param fid Identifier for the transaction. Should match the {@code fid} that you passed to
     * {@link #validateAndStartModule(String, long[])} (or {@code null} if you passed that).
     * @throws DocShifterLicenseException Something went wrong while trying to reach the Nalpeiron analytics server.
     */
    public void endModule(String moduleId, Map<String, Object> clientData, long[] fid) throws DocShifterLicenseException {
        if (!helper.isPassiveActivation()) {
            //call end feature
            helper.stopFeature(NalpeironHelper.NALPEIRON_USERNAME, moduleId, clientData, fid);
        }
    }

    /**
     * Checks if the current license is entitled to a specific module, without registering a usage of it.
     * @param moduleId The module/feature to check.
     * @return {@code true} if the license has access, {@code false} otherwise.
     * @throws DocShifterLicenseException Something horrible went wrong internally in Nalpeiron.
     */
    public boolean hasModuleAccess(String moduleId) throws DocShifterLicenseException {
        return helper.getFeatureStatus(moduleId) == NalpeironHelper.FeatureStatus.AUTHORIZED;
    }

    /**
     * Checks if the license is permanent
     * @return if the license is permanent or not
     * @throws DocShifterLicenseException Something went wrong while trying to fetch this information.
     */
    public boolean isLicensePermanent() throws DocShifterLicenseException {

        NalpeironHelper.LicenseType licenseType = helper.getLicenseType();

        return NalpeironHelper.LicenseType.PERMANENT.equals(licenseType)
                || NalpeironHelper.LicenseType.CONCURRENT_PERMANENT.equals(licenseType);
    }

    /**
     * Gets the subscription expiration date of the current license
     * or a date far away in the future for a permanent license
     * @return An expiration date relative to the local time of the machine.
     * @throws DocShifterLicenseException Something went wrong while trying to fetch this information.
     */
    public LocalDateTime getSubscriptionExpirationDate() throws DocShifterLicenseException {
        return helper.getSubscriptionExpirationDate();
    }

    /**
     * Gets the maintenanceExpirationDate
     * @return An maintenance date relative to the local time of the machine.
     * @throws DocShifterLicenseException exception while trying to get the date
     */
    public LocalDateTime getMaintenanceExpirationDate() throws DocShifterLicenseException {
        return helper.getMaintenanceExpirationDate();
    }

    /**
     * Gets the lease date of the current license. For an online active activation, the lease date signifies the date
     * when the component will report back to the Nalpeiron licensing server. For an offline active activation, this
     * date means that the offline activation will be invalidated then (regardless of expiration date set) so the
     * customer will have to run through the offline activation procedure again to check in with the Nalpeiron
     * licensing server.
     * TODO: verify what this date means for an offline passive license. Does that mean the certificate has expired
     *  and a new one needs to be provided?
     * @return A lease date relative to the local time of the machine.
     * @throws DocShifterLicenseException Something went wrong while trying to fetch this information.
     */
    public LocalDateTime getLeaseExpirationDate() throws DocShifterLicenseException {
        return helper.getLeaseExpirationDate();
    }

    /**
     * Gets the max receivers allowed.
     * @Return {@code String} the number of max receivers allowed.
     * @throws DocShifterLicenseException Something went wrong while fetching the value of the TAA field.
     */
    public String getMaxReceivers() throws DocShifterLicenseException {
        return helper.getUDFValue(NalpeironHelper.MAX_RECEIVERS_UDF_KEY);
    }

    /**
     * Gets all the licensed modules for the license
     * @return {@code String} list with the name of the licensed modules.
     * @throws DocShifterLicenseException Something went wrong while getting the feature status.
     */
    public List<String> getLicensedModules(List<Module> modules) throws DocShifterLicenseException {

        List<String> modulesLicensed = new ArrayList<>();

        for (Module module : modules) {

            if (module.getCode() == null) {
                continue;
            }

            NalpeironHelper.FeatureStatus featureStatus = helper.getFeatureStatus(module.getCode());

            if (featureStatus.isValid()) {
                modulesLicensed.add(module.getName());
            }
        }

        modulesLicensed = modulesLicensed.stream().sorted().toList();

        log.debug("Modules licensed: {}", modulesLicensed);

        return modulesLicensed;
    }

    /**
     * Gets the license code
     * @return a String with the license code number
     */
    public String getLicenseCode() {
        return helper.getLicenseCode();
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
            if (containerClusterer != null) {
                String lastComputerId = helper.getCachedComputerId();
                try {
                    helper.returnLicense(licenseCode);
                    FileUtils.deleteLineOrFileIfEmpty(persistentLicPath, lastComputerId);
                } catch (Exception ex) {
                    log.warn("Could not return license {} correctly!", licenseCode, ex);
                    if (!checkComputerId()) {
                        log.info("Will keep {} in {} so it can be cleaned on next startup", lastComputerId, persistentLicPath);
                    }
                }
            }
        }

        //Cleanup and shutdown library
        helper.closeNalpLibrary();

        //remove helper from assigned memory
        helper = null;
    }
}
