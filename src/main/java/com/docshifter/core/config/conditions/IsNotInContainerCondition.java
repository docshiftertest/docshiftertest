package com.docshifter.core.config.conditions;

import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.context.annotation.Conditional;

public class IsNotInContainerCondition extends NoneNestedConditions {
	public IsNotInContainerCondition() {
		super(ConfigurationPhase.REGISTER_BEAN);
	}

	@Conditional(IsInContainerCondition.class)
	static class ContainerCondition {}
}
