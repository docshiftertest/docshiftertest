package com.docshifter.core.config.service;

import org.apache.commons.lang.StringUtils;
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

	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ILicensingService.class.getName());
	private static final Map<String, Date> keys = new HashMap<>();
	static {
		keys.put("b6b1edf7-8e7c-4209-9911-8f630114be4d", null);
		keys.put("c0414684-ac05-4bcd-8810-c54f8e00e52a", new GregorianCalendar(2019, Calendar.JUNE, 30, 23, 59, 59).getTime());
		keys.put("6cd941e1-2549-4bc6-91c1-b6c26f466bfc", new GregorianCalendar(2019, Calendar.OCTOBER, 15, 23, 59, 59).getTime());
		keys.put("cea963d8-a2a9-4926-8493-f52121a46101", new GregorianCalendar(2019, Calendar.NOVEMBER, 30, 23, 59, 59).getTime());
	}

	public MockLicensingService() {
		logger.info("Container environment detected.");
		String licenseCode = System.getenv("DS_LICENSE_CODE");

		if (StringUtils.isBlank(licenseCode)) {
			logger.fatal("No license code found. Make sure you have set the DS_LICENSE_CODE environment variable.");
			System.exit(0);
		}

		if (!keys.containsKey(licenseCode)) {
			logger.fatal(licenseCode + " is an invalid license code.");
			System.exit(0);
		}

		Date expiryDate = keys.get(licenseCode);
		if (expiryDate != null && new Date().compareTo(expiryDate) > 0) {
			logger.fatal("License code " + licenseCode + " has expired.");
			System.exit(0);
		}

		logger.info("License validated.");
	}

	@Override
	public long[] validateAndStartModule(String moduleId, long[] fid) {
		return fid;
	}

	@Override
	public void endModule(String moduleId, Map<String, Object> clientData, long[] fid) {
	}
}