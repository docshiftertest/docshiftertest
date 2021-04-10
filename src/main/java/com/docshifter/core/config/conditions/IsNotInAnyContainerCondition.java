package com.docshifter.core.config.conditions;

import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.context.annotation.Conditional;

public class IsNotInAnyContainerCondition extends NoneNestedConditions {
	public IsNotInAnyContainerCondition() {
		super(ConfigurationPhase.REGISTER_BEAN);
	}

	@Conditional(IsInAnyContainerCondition.class)
	static class AnyContainerCondition {}
}
