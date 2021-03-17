package com.docshifter.core.config.services;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile("licensing")
@Conditional(IsInDockerCondition.class)
public class MockLicensingService implements ILicensingService {

	private static final Logger log = LogManager.getLogger(ILicensingService.class);
	private static final Map<String, Date> keys = new HashMap<>();
	static {
		keys.put("b6b1edf7-8e7c-4209-9911-8f630114be4d", null);
		keys.put("f5c1668c-f587-475c-b6d1-b7ee2b5cbacd", new GregorianCalendar(2021, Calendar.MARCH, 31, 23, 59, 59).getTime());
		keys.put("5f4f7686-e502-4a89-aa64-834f0220107e", new GregorianCalendar(2021, Calendar.JUNE, 30, 23, 59, 59).getTime());
		keys.put("2beb15ee-8292-42b2-85f1-dab53d9f5255", new GregorianCalendar(2021, Calendar.SEPTEMBER, 30, 23, 59, 59).getTime());
		keys.put("11206e6e-694b-43c7-906c-209134d3fadc", new GregorianCalendar(2021, Calendar.DECEMBER, 31, 23, 59, 59).getTime());
		keys.put("22f37141-f6b1-4478-ae53-43ae15aae19a", new GregorianCalendar(2022, Calendar.MARCH, 31, 23, 59, 59).getTime());
		keys.put("edd36cc0-9bf5-4c21-bf00-91e48f2e521d", new GregorianCalendar(2022, Calendar.JUNE, 30, 23, 59, 59).getTime());
		keys.put("006208ca-2054-4175-a6bd-c406ebb0d8df", new GregorianCalendar(2022, Calendar.SEPTEMBER, 30, 23, 59, 59).getTime());
		keys.put("b0b6d35c-fe3f-45fb-b795-24bc3453263d", new GregorianCalendar(2022, Calendar.DECEMBER, 31, 23, 59, 59).getTime());
	}

	private final String licenseCode;
	private final Date expiryDate;

	public MockLicensingService() {
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

		expiryDate = keys.get(licenseCode);
		checkLicense();

		log.info("License validated.");
	}

	@Override
	public long[] validateAndStartModule(String moduleId, long[] fid) {
		checkLicense();
		return fid;
	}

	private void checkLicense() {
		if (expiryDate != null && new Date().compareTo(expiryDate) > 0) {
			log.fatal("License code {} has expired.", licenseCode);
			System.exit(0);
		}
	}

	@Override
	public void endModule(String moduleId, Map<String, Object> clientData, long[] fid) {
	}
}
