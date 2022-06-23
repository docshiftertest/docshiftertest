package com.docshifter.core.config.services;

import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.NetworkUtils;

import java.util.Set;

/**
 * Utilities that can be used for discovery of components/instances in a container cluster and to enable clustering
 * between them.
 */
public interface IContainerClusterer {
	/**
	 * Lists all the replicas of a component in a cluster, besides the current replica (if applicable).
	 * @param hostname A known hostname to check. The appropriate component type will be deduced from this hostname.
	 * @return A set of replicas mapped by their names that are active in the cluster, EXCLUDING the
	 * current instance (if the component type matches the one of the current application).
	 * @throws DocShifterLicenseException If an error occurred while fetching the list of replicas.
	 */
	Set<String> listOtherReplicas(String hostname) throws DocShifterLicenseException;

	/**
	 * Lists all the replicas of this particular component in a cluster, besides the current replica.
	 * @return A set of replicas mapped by their names that are active in the cluster, EXCLUDING the
	 * current instance.
	 * @throws DocShifterLicenseException If an error occurred while fetching the list of replicas.
	 */
	default Set<String> listOtherReplicas() throws DocShifterLicenseException {
		return listOtherReplicas(NetworkUtils.getLocalHostName());
	}
}
