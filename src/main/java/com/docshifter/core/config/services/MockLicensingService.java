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
		// 2023 Non-production usage with Insights
		keys.put("f5abaef5-bf9f-415a-84e8-a41b870a9c7b", new License(
				new GregorianCalendar(2023, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2023 Production usage with Insights
		keys.put("735d1db5-cb8e-4431-b599-0228f97c721e", new License(
				new GregorianCalendar(2023, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2023 Non-production usage with Insights + OCR
		keys.put("565c9dc8-3650-42eb-8de7-5495b489659e", new License(
				new GregorianCalendar(2023, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Transformation_OCR, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2023 Production usage with Insights + OCR
		keys.put("f9cb115e-4cae-45bb-919d-5a643b317750", new License(
				new GregorianCalendar(2023, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Transformation_OCR, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2024 Non-production usage with Insights
		keys.put("b21f20e3-2dc0-4e12-a226-6ae67060a392", new License(
				new GregorianCalendar(2024, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2024 Production usage with Insights
		keys.put("dae3513b-f1d5-422e-a03d-dbfab6b8e548", new License(
				new GregorianCalendar(2024, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2024 Non-production usage with Insights + OCR
		keys.put("667b3146-9936-40f9-853a-6c7fff708d22", new License(
				new GregorianCalendar(2024, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Transformation_OCR, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2024 Production usage with Insights + OCR
		keys.put("b739b836-e914-43b4-a2b4-2d636cfdc224", new License(
				new GregorianCalendar(2024, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Transformation_OCR, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2025 Non-production usage with Insights
		keys.put("cb1c95c9-7d9d-414a-9bad-ed1eeb4e900d", new License(
				new GregorianCalendar(2025, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				8, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2025 Production usage with Insights
		keys.put("0f4b11cf-fbfb-4837-b4dc-e12ddf7a7afc", new License(
				new GregorianCalendar(2025, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2025 Non-production usage with Insights + OCR
		keys.put("233d1b50-a99f-4197-b6c7-3d0efb89d082", new License(
				new GregorianCalendar(2025, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Transformation_OCR, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));
		// 2025 Production usage with Insights + OCR
		keys.put("06ce52e2-4f76-4c20-a7c7-32b337a6a3ac", new License(
				new GregorianCalendar(2025, Calendar.APRIL, 21, 23, 59, 59).getTime(),
				2, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Metrics, LicensableModule.Transformation_OCR, LicensableModule.Release_Documentum_Export, LicensableModule.Release_Documentum_New_Rendition));

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

		// === Gulfstream ===
		// CAD MODULE TRIAL (VALID FOR 1 WEEK + 1 EXTRA WEEK AS MARGIN)
		keys.put("b115be2b-0ee3-429f-b48b-63d57bb902c2", new License(
				new GregorianCalendar(2021, Calendar.DECEMBER, 26, 23,	59,	59).getTime(),
				1, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Transformation_CAD));
		// Non-production usage
		keys.put("265a797a-bc7a-4208-8176-5d86c969222a", new License(
				new GregorianCalendar(2022, Calendar.SEPTEMBER, 31, 23,	59,	59).getTime(),
				1, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// Production usage
		keys.put("fe08d717-f8e8-4aa0-b641-9ed734f8cde7", new License(
				new GregorianCalendar(2022, Calendar.SEPTEMBER, 31, 23,	59,	59).getTime(),
				1, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// Non-production usage with CAD, in case they do decide to move forward
		keys.put("0319ae02-13d5-45b2-8f2c-6baa8229e590", new License(
				new GregorianCalendar(2022, Calendar.SEPTEMBER, 31, 23,	59,	59).getTime(),
				1, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Transformation_CAD));
		// Production usage with CAD, in case they do decide to move forward
		keys.put("9680c8af-8ce3-42ac-86ad-463f67abf6dd", new License(
				new GregorianCalendar(2022, Calendar.SEPTEMBER, 31, 23,	59,	59).getTime(),
				1, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Transformation_CAD));

		// === Volksbank July 1 2022 expiry ===
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("c9ed93e3-4400-43d4-8c2c-094dde86dbee", new License(
				new GregorianCalendar(2022, Calendar.JULY, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));

		// === Volksbank July 1 2023 expiry ===
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("5ce91f09-d48e-4284-a46e-135e856fd44e", new License(
				new GregorianCalendar(2023, Calendar.JULY, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));

		// === Volksbank July 1 2024 expiry ===
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("96d61229-d76c-40e0-80d7-3d95bd35f842", new License(
				new GregorianCalendar(2024, Calendar.JULY, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));

		// === Volksbank July 1 2025 expiry ===
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("5d2f87db-33da-4eed-a33a-028b8c4768be", new License(
				new GregorianCalendar(2025, Calendar.JULY, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));

		// === SPARE CODES (PoCs, new customers,...) ===
		// TODO: ***** Be CAREFUL ***** We just alternate Base, HiFi and Everything profiles here as examples
		// You can also merge a set (Base, HiFi, Advanced...) of Modules with individual extra Modules... e.g.
		//	... MockLicensingModuleSet.DocShifterHiFi.getLicensableModules(), LicensableModule.Custom_module, LicensableModule.Metrics));
		keys.put("932c4745-9cab-4937-b704-236beb5dbb93", new License(
				new GregorianCalendar(2021, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("11206e6e-694b-43c7-906c-209134d3fadc", new License(
				new GregorianCalendar(2021, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("c87c72b0-07e3-4e0d-9164-7526d6017e01", new License(
				new GregorianCalendar(2021, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
		keys.put("b0c46468-8476-42ac-8304-6e19316ff938", new License(
				new GregorianCalendar(2022, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("f760beff-3023-47f4-a6af-494b93862a20", new License(
				new GregorianCalendar(2022, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("570cdead-c536-4f08-9483-18e487459ea6", new License(
				new GregorianCalendar(2022, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
		keys.put("ae782ac9-4706-4bf4-8cb8-489a896a212f", new License(
				new GregorianCalendar(2022, Calendar.JUNE, 30, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("edd36cc0-9bf5-4c21-bf00-91e48f2e521d", new License(
				new GregorianCalendar(2022, Calendar.JUNE, 30, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("49a69f50-d473-4397-aad9-ee97d757da8e", new License(
				new GregorianCalendar(2022, Calendar.JUNE, 30, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
		keys.put("e3dbd68b-96b3-4b21-8a27-1bc28d3f4ed2", new License(
				new GregorianCalendar(2022, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("397d459e-6fe3-4869-bda6-c40d0bf86971", new License(
				new GregorianCalendar(2022, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("0c5fd5af-8a84-4e10-91c5-a3df5fdfb3a1", new License(
				new GregorianCalendar(2022, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
		keys.put("abcf2659-895f-4a85-982e-1b4032d46254", new License(
				new GregorianCalendar(2022, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("b2066068-5816-4a71-af8a-58685d8d3ab7", new License(
				new GregorianCalendar(2022, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("ad40bf68-665f-4d39-aaff-2de2c6e563bc", new License(
				new GregorianCalendar(2022, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
		keys.put("dfa4d4e4-39c1-424c-b717-305b99f1eb71", new License(
				new GregorianCalendar(2023, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("d0763080-8751-4942-a262-102ec5c29122", new License(
				new GregorianCalendar(2023, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("68e38b82-263d-4f5b-aeac-fb6c21006757", new License(
				new GregorianCalendar(2023, Calendar.MARCH, 31, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
		keys.put("9f01ba19-f9ec-40ee-a619-400551b1bd07", new License(
				new GregorianCalendar(2023, Calendar.JUNE, 30, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("5e995acd-77ca-4fea-a487-b060eceb096a", new License(
				new GregorianCalendar(2023, Calendar.JUNE, 30, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("55c15ab5-9273-4d60-8258-e8162efc1083", new License(
				new GregorianCalendar(2023, Calendar.JUNE, 30, 23, 59, 59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
		keys.put("c76f6d84-7e35-46e2-a608-978d08bf6d72", new License(
				new GregorianCalendar(2023, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("92b89aa4-f34d-4262-9181-fa9a3f3efe1a", new License(
				new GregorianCalendar(2023, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("46d3a9a1-8b17-4bd5-a244-556d37be4b51", new License(
				new GregorianCalendar(2023, Calendar.SEPTEMBER, 30, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
		keys.put("a89cc318-9a57-4e9a-ae48-98eaee70da96", new License(
				new GregorianCalendar(2023, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("a0136286-5f01-49c4-9441-96d5c0e159bc", new License(
				new GregorianCalendar(2023, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("8c11d245-1834-4ac6-8e56-1b12c56b12d9", new License(
				new GregorianCalendar(2023, Calendar.DECEMBER, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
		keys.put("6a4dcb38-5713-4d14-8c2a-a01ba50922f4", new License(
				new GregorianCalendar(2024, Calendar.MARCH, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterBase.getLicensableModules()));
		keys.put("3a8aab91-ecf7-4b03-93c3-ca2b444810a6", new License(
				new GregorianCalendar(2024, Calendar.MARCH, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterHiFi.getLicensableModules()));
		// DocShifterEverything = Give me EVERYTHING for Licensable Modules!
		// Note we don't give DEV here, as DEV is everything we've written and everything we will write!
		keys.put("7d0525a7-614e-42e9-b7ce-cbf2fd9512bd", new License(
				new GregorianCalendar(2024, Calendar.MARCH, 31, 23,	59,	59).getTime(),
				null, MockLicensingModuleSet.DocShifterEverything.getLicensableModules()));
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
