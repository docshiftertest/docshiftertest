package com.docshifter.core.config.service;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("licensing")
@Conditional(IsInDockerCondition.class)
public class MockLicensingService implements ILicensingService {

	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ILicensingService.class.getName());

	public MockLicensingService() {
		logger.info("Container environment detected. Licensing service disabled, make sure you are authorized to use this software.");
	}

	@Override
	public long[] validateAndStartModule(String moduleId, long[] fid) {
		return fid;
	}

	@Override
	public void endModule(String moduleId, Map<String, Object> clientData, long[] fid) {
	}
}
