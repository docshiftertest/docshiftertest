package com.docshifter.core.config.conditions;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.context.annotation.Conditional;

public class IsInAnyContainerCondition extends AnyNestedCondition {
	public IsInAnyContainerCondition() {
		super(ConfigurationPhase.REGISTER_BEAN);
	}

	@Conditional(IsInKubernetesCondition.class)
	static class KubernetesCondition {}

	@Conditional(IsInGenericContainerCondition.class)
	static class GenericCondition {}
}
