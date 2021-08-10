package com.docshifter.core.config.services;

import com.docshifter.core.config.conditions.IsInAnyContainerCondition;
import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.nalpeiron.LicensableModule;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile("licensing")
@Conditional(IsInAnyContainerCondition.class)
public class MockLicensingService implements ILicensingService {

	private static final Logger log = LogManager.getLogger(ILicensingService.class);
	private static final Map<String, License> keys = new HashMap<>();
	static {
		// Also see https://docshifter.atlassian.net/wiki/spaces/TEC/pages/1714716673/Environment+Overview
		// Make sure to keep this up to date!

		// === THE ROOTEST OF ROOT CODE, NEVER SEND THIS TO ANYONE, KEEP IT FOR INTERNAL DEVELOPMENT! ===
		keys.put("66de59a9-2de1-4621-b5a3-6778ad7c4eb8", new License(
				null,
				null, MockLicensingModuleSet.DEV.getLicensableModules()));

		// === PostFinance ===
		// Non-production usage
		keys.put("006208ca-2054-4175-a6bd-c406ebb0d8df", new License(
				new GregorianCalendar(2023, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				5, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// Production usage
		keys.put("b0b6d35c-fe3f-45fb-b795-24bc3453263d", new License(
				new GregorianCalendar(2023, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));

		// === Novartis ===
		// Non-production usage
		keys.put("bcccbf81-0a79-4da8-bd44-4abbc0ea85d9", new License(
				null,
				10, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// Production usage
		keys.put("855408d7-cd0d-4cbf-96e7-64efa6f6fefa", new License(
				null,
				4, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));

		// === Preyer ===
		keys.put("b1656ce3-6c9b-4382-895c-429378c37632", new License(
				new GregorianCalendar(2023, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));

		// === Bayer ===
		keys.put("2ad776f1-d715-4f39-a74b-c007f62228f1", new License(
				null,
				1, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));

		// === Lorenz ===
		// TODO: Check Module list w/ Paul
		keys.put("22f37141-f6b1-4478-ae53-43ae15aae19a", new License(
				new GregorianCalendar(2022, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));

		// === Volksbank ===
		// DEV = Give me EVERYTHING for Licensable Modules!
		keys.put("2beb15ee-8292-42b2-85f1-dab53d9f5255", new License(
				new GregorianCalendar(2021, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));

		// === Volksbank July 1 2022 expiry ===
		// DEV = Give me EVERYTHING for Licensable Modules!
		keys.put("c9ed93e3-4400-43d4-8c2c-094dde86dbee", new License(
				new GregorianCalendar(2022, Calendar.JULY, 1, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));

		// === Volksbank July 1 2023 expiry ===
		// DEV = Give me EVERYTHING for Licensable Modules!
		keys.put("5ce91f09-d48e-4284-a46e-135e856fd44e", new License(
				new GregorianCalendar(2023, Calendar.JULY, 1, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));

		// === Volksbank July 1 2024 expiry ===
		// DEV = Give me EVERYTHING for Licensable Modules!
		keys.put("96d61229-d76c-40e0-80d7-3d95bd35f842", new License(
				new GregorianCalendar(2024, Calendar.JULY, 1, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));

		// === Volksbank July 1 2025 expiry ===
		// DEV = Give me EVERYTHING for Licensable Modules!
		keys.put("5d2f87db-33da-4eed-a33a-028b8c4768be", new License(
				new GregorianCalendar(2025, Calendar.JULY, 1, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));

		// === SPARE CODES (PoCs, new customers,...) ===
		// TODO: ***** Be CAREFUL ***** We just alternate Base and HiFi profiles here as examples
		// You can also merge a set (Base, HiFi, Advanced...) of Modules with individual extra Modules... e.g.
		//	... MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Custom_module, LicensableModule.Metrics));
		keys.put("5f4f7686-e502-4a89-aa64-834f0220107e", new License(
				new GregorianCalendar(2021, Calendar.JUNE, 30, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		keys.put("5a4da8c5-e7a8-43d9-934f-bad86f77e4d8", new License(
				new GregorianCalendar(2021, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("11206e6e-694b-43c7-906c-209134d3fadc", new License(
				new GregorianCalendar(2021, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		keys.put("b0c46468-8476-42ac-8304-6e19316ff938", new License(
				new GregorianCalendar(2022, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("edd36cc0-9bf5-4c21-bf00-91e48f2e521d", new License(
				new GregorianCalendar(2022, Calendar.JUNE, 30, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		keys.put("e3dbd68b-96b3-4b21-8a27-1bc28d3f4ed2", new License(
				new GregorianCalendar(2022, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("fe08d717-f8e8-4aa0-b641-9ed734f8cde7", new License(
				new GregorianCalendar(2022, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		keys.put("dfa4d4e4-39c1-424c-b717-305b99f1eb71", new License(
				new GregorianCalendar(2023, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("5e995acd-77ca-4fea-a487-b060eceb096a", new License(
				new GregorianCalendar(2023, Calendar.JUNE, 30, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		keys.put("c76f6d84-7e35-46e2-a608-978d08bf6d72", new License(
				new GregorianCalendar(2023, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("a0136286-5f01-49c4-9441-96d5c0e159bc", new License(
				new GregorianCalendar(2023, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
	}

	private final KubernetesClient k8sClient;
	private final String licenseCode;
	private final License licenseInfo;

	@Autowired(required = false)
	public MockLicensingService(KubernetesClient k8sClient) {
		log.info("Container environment detected.");
		licenseCode = System.getenv("DS_LICENSE_CODE");

		if (StringUtils.isBlank(licenseCode)) {
			log.fatal("No license code found. Make sure you have set the DS_LICENSE_CODE environment variable.");
			System.exit(0);
		}

		if (!keys.containsKey(licenseCode)) {
			log.fatal( "{} is an invalid license code.", licenseCode);
			System.exit(0);
		}

		licenseInfo = keys.get(licenseCode);
		this.k8sClient = k8sClient;
		// On application startup, also make sure the customer is playing nice... (i.e. not overscaling the number of
		// receivers based on what their license allows)
		checkLicense();
		checkPods();

		log.info("License validated.");
	}

	public MockLicensingService() {
		this(null);
	}

	@Override
	public long[] validateAndStartModule(String moduleId, long[] fid) throws DocShifterLicenseException {
		checkLicense();
		// Do no further checks if we aren't given a ModuleId, we're probably in the init check for overall licence expiry
		if (moduleId == null) {
			return fid;
		}
		// If not expired, let's check the Module Id
		if (!licenseInfo.licensedModules.contains(moduleId)) {
			// This is also used for checking if Metrics is licensed, although it's not strictly a Module...
			throw new DocShifterLicenseException("Module: " + moduleId + " is not licensed. Please contact DocShifter for assistance");
		}
		return fid;
	}

	private void checkLicense() {
		if (licenseInfo.expiry != null && new Date().compareTo(licenseInfo.expiry) > 0) {
			log.fatal("License code {} has expired.", licenseCode);
			System.exit(0);
		}
	}

	private void checkPods() {
		if (k8sClient == null || licenseInfo.maxReplicas == null) {
			return;
		}

		String currPod = System.getenv("HOSTNAME");
		String currRs = currPod.substring(0, currPod.lastIndexOf('-'));
		String currDeploy = currRs.substring(0, currRs.lastIndexOf('-'));
		String currNs = k8sClient.getConfiguration().getNamespace();
		Integer replicas = null;
		try {
			replicas = k8sClient.apps()
					.deployments()
					.inNamespace(currNs)
					.withName(currDeploy)
					.get()
					.getSpec()
					.getReplicas();
			if (replicas == null) {
				throw new NullPointerException("Kubernetes API request returned a NULL value!");
			}
		} catch (Exception ex) {
			log.fatal("Unable to query the Kubernetes API correctly. Did you provide the service account with the" +
					" appropriate credentials to GET a Deployment?", ex);
			System.exit(0);
		}
		if (replicas > licenseInfo.maxReplicas) {
			log.fatal("You have {} receivers running. This is more than allotted for your current license ({}). " +
							"Please downscale the number of pods or upgrade your license to continue.", replicas,
					licenseInfo.maxReplicas);
			System.exit(0);
		}
	}

	@Override
	public void endModule(String moduleId, Map<String, Object> clientData, long[] fid) {
	}

	private static class License {
		private final Date expiry;
		private final Integer maxReplicas;
		private final Set<String> licensedModules;

		public License(Date expiry, Integer maxReplicas, LicensableModule... licensedModules) {
			this.expiry = expiry;
			this.maxReplicas = maxReplicas;
			this.licensedModules = Collections.unmodifiableSet(Arrays.stream(licensedModules).map(LicensableModule::toString).collect(Collectors.toSet()));
		}

		public License(Date expiry, Integer maxReplicas, LicensableModule[] licensedModules, LicensableModule... extraLicensedModules) {
			this.expiry = expiry;
			this.maxReplicas = maxReplicas;
			Set<String> licensedModulesWorking = Arrays.stream(licensedModules).map(LicensableModule::toString).collect(Collectors.toSet());
			licensedModulesWorking.addAll(Arrays.stream(extraLicensedModules).map(LicensableModule::toString).collect(Collectors.toSet()));
			this.licensedModules = Collections.unmodifiableSet(licensedModulesWorking);
		}
	}
}
