package com.docshifter.core.config.services;

import com.docshifter.core.config.conditions.IsInKubernetesCondition;
import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Checks cluster size in a Kubernetes environment.
 */
@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
@Service
@Conditional(IsInKubernetesCondition.class)
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
public class KubernetesChecker implements IContainerChecker {

	private final KubernetesClient k8sClient;

	public KubernetesChecker(KubernetesClient k8sClient) {
		this.k8sClient = k8sClient;
	}

	public void checkReplicas(int maxReplicas) throws DocShifterLicenseException {
		if (maxReplicas <= 0) {
			return;
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
				throw new DocShifterLicenseException("Kubernetes API request returned a NULL value!");
			}
		} catch (Exception ex) {
			throw new DocShifterLicenseException("Unable to query the Kubernetes API correctly. Did you provide the service account with the" +
					" appropriate credentials to GET a Deployment?", ex);
		}
		if (replicas > maxReplicas) {
			throw new DocShifterLicenseException("You have " + replicas + " receivers running. This is more than " +
					"allotted for your current license (" + maxReplicas + "). Please downscale the number of pods or " +
					"upgrade your license to continue.");
		}
	}
}
