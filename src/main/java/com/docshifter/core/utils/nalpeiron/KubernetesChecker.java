package com.docshifter.core.utils.nalpeiron;

import com.docshifter.core.config.services.ILicensingService;
import com.docshifter.core.exceptions.DocShifterLicenseException;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KubernetesChecker {

	// Don't use Lombok @Log4j2 because we're getting the general logger for the interface instead of this particular
	// class
	private static final Logger log = LogManager.getLogger(ILicensingService.class);

	private final KubernetesClient k8sClient;
	private final NalpeironHelper nalpeironHelper;

	public KubernetesChecker(KubernetesClient k8sClient, NalpeironHelper nalpeironHelper) {
		this.k8sClient = k8sClient;
		this.nalpeironHelper = nalpeironHelper;
	}

	public void checkPods() throws DocShifterLicenseException {
		int maxReplicas = nalpeironHelper.getNumberAvailableSimultaneousLicenses();
		if (maxReplicas <= 0) {
			return;
		}

		String currPod = System.getenv("HOSTNAME");
		String currRs = currPod.substring(0, currPod.lastIndexOf('-'));
		String currDeploy = currRs.substring(0, currRs.lastIndexOf('-'));
		String currNs = k8sClient.getConfiguration().getNamespace();
		Integer replicas = null;
		try {
			replicas = k8sClient.apps()
					.deployments()
					.inNamespace(currNs)
					.withName(currDeploy)
					.get()
					.getSpec()
					.getReplicas();
			if (replicas == null) {
				throw new NullPointerException("Kubernetes API request returned a NULL value!");
			}
		} catch (Exception ex) {
			log.fatal("Unable to query the Kubernetes API correctly. Did you provide the service account with the" +
					" appropriate credentials to GET a Deployment?", ex);
			System.exit(0);
		}
		if (replicas > maxReplicas) {
			log.fatal("You have {} receivers running. This is more than allotted for your current license ({}). " +
							"Please downscale the number of pods or upgrade your license to continue.", replicas, maxReplicas);
			System.exit(0);
		}
	}
}
