package com.docshifter.core.config.conditions;

import java.util.HashSet;
import java.util.Set;

public class IsInGenericContainerCondition extends IsInContainerCondition {
	private static final Set<String> cGroups = new HashSet<>();
	static {
		cGroups.add("docker"); // Docker
		cGroups.add("ecs"); // Amazon Elastic Container Service (ECS)
	}

	public IsInGenericContainerCondition() {
		super(cGroups);
	}
}
