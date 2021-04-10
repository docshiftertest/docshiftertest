package com.docshifter.core.config.conditions;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Conditional;

import java.util.HashSet;
import java.util.Set;

public class IsInKubernetesCondition extends AllNestedConditions {
	public IsInKubernetesCondition() {
		super(ConfigurationPhase.REGISTER_BEAN);
	}

	@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
	static class SpringCondition {}

	@Conditional(CustomNested.class)
	static class CustomCondition {}

	private static class CustomNested extends IsInContainerCondition {
		private static final Set<String> cGroups = new HashSet<>();
		static {
			cGroups.add("kubepods"); // Kubernetes
		}

		public CustomNested() {
			super(cGroups);
		}
	}
}
