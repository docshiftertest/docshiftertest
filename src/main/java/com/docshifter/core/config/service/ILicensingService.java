package com.docshifter.core.config.service;

import com.docshifter.core.exceptions.DocShifterLicenseException;

import java.util.Map;

public interface ILicensingService {
	long[] validateAndStartModule(String moduleId, long[] fid) throws DocShifterLicenseException;
	void endModule(String moduleId, Map<String, Object> clientData, long[] fid) throws DocShifterLicenseException;
}
