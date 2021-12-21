package com.docshifter.core.utils.nalpeiron;

import com.docshifter.core.config.services.ILicensingService;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Checks cluster size in a Kubernetes environment.
 */
public class KubernetesChecker implements IContainerChecker {

	// Don't use Lombok @Log4j2 because we're getting the general logger for the interface instead of this particular
	// class
	private static final Logger log = LogManager.getLogger(ILicensingService.class);

	private final KubernetesClient k8sClient;

	public KubernetesChecker(KubernetesClient k8sClient) {
		this.k8sClient = k8sClient;
	}

	public boolean checkReplicas(int maxReplicas) {
		if (maxReplicas <= 0) {
			return true;
		}

		String currPod = System.getenv("HOSTNAME");
		String currRs = currPod.substring(0, currPod.lastIndexOf('-'));
		String currDeploy = currRs.substring(0, currRs.lastIndexOf('-'));
		String currNs = k8sClient.getConfiguration().getNamespace();
		Integer replicas;
		try {
			replicas = k8sClient.apps()
					.deployments()
					.inNamespace(currNs)
					.withName(currDeploy)
					.get()
					.getSpec()
					.getReplicas();
			if (replicas == null) {
				log.error("Kubernetes API request returned a NULL value!");
				return false;
			}
		} catch (Exception ex) {
			log.error("Unable to query the Kubernetes API correctly. Did you provide the service account with the" +
					" appropriate credentials to GET a Deployment?", ex);
			return false;
		}
		if (replicas > maxReplicas) {
			log.error("You have {} receivers running. This is more than allotted for your current license ({}). " +
							"Please downscale the number of pods or upgrade your license to continue.", replicas, maxReplicas);
			return false;
		}
		return true;
	}
}
