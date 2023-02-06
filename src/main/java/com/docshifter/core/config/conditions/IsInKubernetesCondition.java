package com.docshifter.core.config.conditions;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Conditional;

import java.util.Set;

public class IsInKubernetesCondition extends AllNestedConditions {
	public IsInKubernetesCondition() {
		super(ConfigurationPhase.REGISTER_BEAN);
	}

	@Conditional(IsInContainerCondition.class)
	static class ContainerCondition {}

	@Conditional(AnyKubernetesCluesFound.class)
	static class KubernetesCondition {}

	private static class AnyKubernetesCluesFound extends AnyNestedCondition {
		public AnyKubernetesCluesFound() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
		static class SpringCondition {}

		@Conditional(KubernetesCgroupCondition.class)
		static class CustomCondition {}

		private static class KubernetesCgroupCondition extends CgroupCondition {
			private static final Set<String> cGroups = Set.of("kubepods");

			@Override
			protected Set<String> getCgroups() {
				return cGroups;
			}
		}
	}
}
