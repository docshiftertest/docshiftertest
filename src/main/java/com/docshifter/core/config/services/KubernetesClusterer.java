package com.docshifter.core.config.services;

import com.docshifter.core.config.conditions.IsInKubernetesCondition;
import com.docshifter.core.exceptions.DocShifterLicenseException;
import com.docshifter.core.utils.NetworkUtils;
import com.docshifter.core.utils.nalpeiron.NalpeironHelper;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
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
		// The current name of either the underlying ReplicaSet managing pods for the Deployment, e.g.
		// receiver-596c884f74, or the StatefulSet itself, e.g. mq
		final String hostnameWithoutLastDash = hostname.substring(0, lastIndexOfDash);
		String controllerName = hostnameWithoutLastDash;
		// Assume we're dealing with a Deployment by default, but a hostname with two hyphens or more might be a
		// StatefulSet as well! E.g. message-queue-0 might be a pod managed by a StatefulSet controller named
		// message-queue
		boolean isDeployment = true;
		if ((lastIndexOfDash = controllerName.lastIndexOf('-')) >= 0){
			// The current Deployment controller, e.g. receiver
			controllerName = controllerName.substring(0, lastIndexOfDash);
		} else {
			// If the hostname contained only one hyphen, then it's likely a StatefulSet
			isDeployment = false;
		}
		if (cachedReplicasPerComponent.containsKey(controllerName)) {
			log.debug("Using {} data for matching hostname {} from cache", controllerName, hostname);
			return cachedReplicasPerComponent.get(controllerName);
		}
		// The current namespace, e.g. docshifter
		String currNs = k8sClient.getConfiguration().getNamespace();
		log.debug("Deduced controller name {} from hostname {}, current namespace is {}", controllerName, hostname,
				currNs);
		List<Pod> pods;
		try {
			PodTemplateSpec template = null;
			if (isDeployment) {
				Deployment deployment = k8sClient.apps()
						.deployments()
						.inNamespace(currNs)
						.withName(controllerName)
						.get();
				if (deployment != null) {
					template = deployment.getSpec().getTemplate();
				}
			}
			if (template == null) {
				if (isDeployment) {
					// If we thought it was a Deployment before, but we couldn't get one, deduce the potential
					// StatefulSet name from the hostname and try getting that instead as a last resort...
					log.debug("Deployment {} was not found! Checking if there is perhaps a StatefulSet named {}",
							controllerName,	hostnameWithoutLastDash);
					controllerName = hostnameWithoutLastDash;
				}
				StatefulSet statefulSet = k8sClient.apps()
						.statefulSets()
						.inNamespace(currNs)
						.withName(controllerName)
						.get();
				if (statefulSet != null) {
					template = statefulSet.getSpec().getTemplate();
					isDeployment = false;
				}
			}
			final String controllerType = isDeployment ? "Deployment" : "StatefulSet";
			if (template == null) {
				throw new NullPointerException("We looked for a " + controllerType + " with name \"" + controllerName +
						"\" in namespace \"" + currNs + "\" but nothing was found! Could \"" + hostname + "\" be running " +
						"as a standalone pod or was the " + controllerType + " recently renamed?");
			}
			log.debug("Located a {} with name {} in namespace {}", controllerType, controllerName, currNs);
			String appLabel = template.getMetadata().getLabels().get("app");
			if (appLabel == null) {
				throw new NullPointerException("We looked for an \"app\" label on the \"" + controllerName +"\" " +
						"template but nothing was found!");
			}
			log.debug("Looking for pods with app label {} that we can cluster with", appLabel);
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
					" appropriate credentials to GET a Deployment/StatefulSet and LIST multiple Pods in the current " +
					"namespace \"" + currNs + "\"? Consult this exception's cause for more details.", ex);
		}
		String currHostname = NetworkUtils.getLocalHostName();
		cachedReplicasPerComponent.put(controllerName, pods.stream()
				.map(Pod::getMetadata)
				.map(ObjectMeta::getName)
				.filter(name -> !currHostname.equals(name))
				.collect(Collectors.toSet()));
		return cachedReplicasPerComponent.get(controllerName);
	}

	/**
	 * Clears the cached Kubernetes API results.
	 */
	public void clearCache() {
		log.debug("Clearing Kubernetes clusterer cache");
		cachedReplicasPerComponent.clear();
	}
}
