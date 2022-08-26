package com.docshifter.core.config.services;

import com.docshifter.core.exceptions.DocShifterLicenseException;

/**
 * Checks and enforces cluster size in a containerized environment.
 */
@FunctionalInterface
public interface IContainerChecker {
	/**
	 * Enforces that there aren't more instances running in the environment than the current license has allotted.
	 * @param maxReplicas The maximum number of replicas to allow.
	 * @throws DocShifterLicenseException The check didn't pass or something went wrong while checking.
	 */
	void performCheck(int maxReplicas) throws DocShifterLicenseException;
}
