package com.docshifter.core.config.service;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class IsNotInDockerCondition extends IsInDockerCondition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return !super.matches(context, metadata);
	}
}
