package com.docshifter.core.config.services;

import com.docshifter.core.config.conditions.IsInKubernetesCondition;
import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks cluster size in a Kubernetes environment.
 */
@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
@Service
@Primary
@Conditional(IsInKubernetesCondition.class)
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
public class KubernetesChecker implements IContainerChecker {

	private final KubernetesClient k8sClient;

	public KubernetesChecker(KubernetesClient k8sClient) {
		this.k8sClient = k8sClient;
	}

	@Override
	public Set<String> checkReplicas(int maxReplicas) throws DocShifterLicenseException {
		// The current pod name (which matches the HOSTNAME), e.g. receiver-596c884f74-775pr
		String currPod = System.getenv("HOSTNAME");
		// The current name of the underlying ReplicaSet managing pods for the Deployment, e.g. receiver-596c884f74
		String currRs = currPod.substring(0, currPod.lastIndexOf('-'));
		// The current Deployment controller, e.g. receiver
		String currDeploy = currRs.substring(0, currRs.lastIndexOf('-'));
		// The current namespace, e.g. docshifter
		String currNs = k8sClient.getConfiguration().getNamespace();
		List<Pod> pods;
		try {
			pods = k8sClient.pods()
					.inNamespace(currNs)
					.withLabel("app", currDeploy)
					.list()
					.getItems();
			if (pods == null) {
				throw new DocShifterLicenseException("Kubernetes API request returned a NULL value!");
			}
		} catch (Exception ex) {
			throw new DocShifterLicenseException("Unable to query the Kubernetes API correctly. Did you provide the service account with the" +
					" appropriate credentials to GET a Deployment?", ex);
		}
		if (maxReplicas > 0 && pods.size() > maxReplicas) {
			throw new DocShifterLicenseException("You have " + pods.size() + " receivers running. This is more than " +
					"allotted for your current license (" + maxReplicas + "). Please downscale the number of pods or " +
					"upgrade your license to continue.");
		}
		return pods.stream()
				.map(Pod::getMetadata)
				.map(ObjectMeta::getName)
				.filter(name -> !currPod.equals(name))
				.collect(Collectors.toSet());
	}
}
