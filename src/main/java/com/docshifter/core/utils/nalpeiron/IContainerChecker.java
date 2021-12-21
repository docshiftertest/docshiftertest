package com.docshifter.core.utils.nalpeiron;

/**
 * Checks cluster size in a containerized environment.
 */
public interface IContainerChecker {
	/**
	 * Checks the number of replicas in a cluster according to a specified maximum.
	 * @param maxReplicas The maximum number of replicas to allow. Zero or a negative value to allow unlimited replicas.
	 * @return True if the number of replicas does not exceed maxReplicas, false otherwise.
	 */
	boolean checkReplicas(int maxReplicas);
}
