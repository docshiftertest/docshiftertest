package com.docshifter.core.config.services;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class IsInDockerCondition implements Condition {
	private static final Set<String> cGroups = new HashSet<>();
	static {
		cGroups.add("docker"); // Docker
		cGroups.add("kubepods"); // Kubernetes
		cGroups.add("ecs"); // Amazon Elastic Container Service (ECS)
	}

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		// The container platform can typically be detected from within a container by analyzing the cgroup file.
		// https://stackoverflow.com/a/52581380 was used as a reference
		try (Stream<String> stream = Files.lines(Paths.get("/proc/1/cgroup"))) {
			return stream.anyMatch(line -> cGroups.stream().anyMatch(cGroup -> line.contains("/" + cGroup)));
		} catch (IOException e) {
			// We're probably just running on Windows here...
			return false;
		}
	}
}
