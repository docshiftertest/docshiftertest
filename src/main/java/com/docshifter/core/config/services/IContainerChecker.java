package com.docshifter.core.config.services;

import com.docshifter.core.exceptions.DocShifterLicenseException;

/**
 * Checks cluster size in a containerized environment.
 */
public interface IContainerChecker {
	/**
	 * Checks the number of replicas in a cluster according to a specified maximum.
	 * @param maxReplicas The maximum number of replicas to allow. Zero or a negative value to allow unlimited replicas.
	 * @throws DocShifterLicenseException If the number of replicas exceeds {@code maxReplicas}, or if an error
	 * occurred while fetching the number of replicas.
	 */
	void checkReplicas(int maxReplicas) throws DocShifterLicenseException;
}
