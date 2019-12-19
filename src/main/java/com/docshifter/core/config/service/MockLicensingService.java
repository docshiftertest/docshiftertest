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
		keys.put("bfd7af58-2266-11ea-843b-98fa9b42e1ca", new GregorianCalendar(2020, Calendar.MARCH, 31, 23, 59, 59).getTime());
		keys.put("b10df310-2266-11ea-b177-98fa9b42e1ca", new GregorianCalendar(2020, Calendar.JUNE, 30, 23, 59, 59).getTime());
		keys.put("a14b8870-2266-11ea-874c-98fa9b42e1ca", new GregorianCalendar(2020, Calendar.OCTOBER, 31, 23, 59, 59).getTime());
		keys.put("8f59e896-2266-11ea-8f74-98fa9b42e1ca", new GregorianCalendar(2020, Calendar.DECEMBER, 31, 23, 59, 59).getTime());
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
