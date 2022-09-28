package com.docshifter.core.config.services;

import com.docshifter.core.config.conditions.IsInKubernetesCondition;
import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.NetworkUtils;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Checks cluster size in a Kubernetes environment.
 */
@Log4j2(topic = NalpeironHelper.LICENSING_IDENTIFIER)
@Service
@Primary
@Conditional(IsInKubernetesCondition.class)
@Profile(NalpeironHelper.LICENSING_IDENTIFIER)
public class KubernetesClusterer implements IContainerClusterer {

	private final KubernetesClient k8sClient;
	private final Map<String, Set<String>> cachedReplicasPerComponent;

	public KubernetesClusterer(KubernetesClient k8sClient) {
		this.k8sClient = k8sClient;
		this.cachedReplicasPerComponent = new ConcurrentHashMap<>();
	}

	@Override
	public Set<String> listOtherReplicas(String hostname) throws DocShifterLicenseException {
		// Hostname matches the current pod name, e.g. receiver-596c884f74-775pr
		int lastIndexOfDash = hostname.lastIndexOf('-');
		if (lastIndexOfDash < 0) {
			throw new DocShifterLicenseException("It appears that you are running the component " + hostname + " as a" +
					" standalone pod, which is not supported for clustering purposes. Please deploy it behind a " +
					"Deployment or StatefulSet controller.");
		}
		// The current name of the underlying ReplicaSet managing pods for the Deployment, e.g. receiver-596c884f74
		String currRs = hostname.substring(0, lastIndexOfDash);
		String currDeploy;
		if ((lastIndexOfDash = currRs.lastIndexOf('-')) >= 0){
			// The current Deployment controller, e.g. receiver
			currDeploy = currRs.substring(0, lastIndexOfDash);
		} else {
			// If the hostname contained only one hyphen, then it's probably a StatefulSet
			currDeploy = currRs;
		}
		if (cachedReplicasPerComponent.containsKey(currDeploy)) {
			return cachedReplicasPerComponent.get(currDeploy);
		}
		// The current namespace, e.g. docshifter
		String currNs = k8sClient.getConfiguration().getNamespace();
		List<Pod> pods;
		try {
			Pod currPod = k8sClient.pods()
					.inNamespace(currNs)
					.withName(hostname)
					.get();
			String appLabel = currPod.getMetadata().getLabels().get("app");
			if (appLabel == null) {
				throw new NullPointerException("We looked for an \"app\" label on \"" + hostname +"\" but nothing was" +
						" found! Could this component be running as a standalone pod?");
			}
			pods = k8sClient.pods()
					.inNamespace(currNs)
					.withLabel("app", appLabel)
					.list()
					.getItems();
			if (pods == null) {
				throw new NullPointerException("Kubernetes API request returned a NULL value!");
			}
		} catch (Exception ex) {
			throw new DocShifterLicenseException("Unable to query the Kubernetes API correctly. Did you provide the service account with the" +
					" appropriate credentials to GET and LIST single/multiple Pods in the current namespace?", ex);
		}
		String currHostname = NetworkUtils.getLocalHostName();
		cachedReplicasPerComponent.put(currDeploy, pods.stream()
				.map(Pod::getMetadata)
				.map(ObjectMeta::getName)
				.filter(name -> !currHostname.equals(name))
				.collect(Collectors.toSet()));
		return cachedReplicasPerComponent.get(currDeploy);
	}

	/**
	 * Clears the cached Kubernetes API results.
	 */
	public void clearCache() {
		cachedReplicasPerComponent.clear();
	}
}
