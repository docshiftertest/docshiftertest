package com.docshifter.core.config.conditions;

import com.sun.istack.NotNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Stream;

public abstract class IsInContainerCondition implements Condition {
	private final Collection<String> cGroups;

	public IsInContainerCondition(Collection<String> cGroups) {
		this.cGroups = cGroups;
	}

	@Override
	public boolean matches(@NotNull ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
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
