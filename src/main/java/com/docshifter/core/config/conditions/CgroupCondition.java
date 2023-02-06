package com.docshifter.core.config.conditions;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

public abstract class CgroupCondition implements Condition {
	protected abstract Set<String> getCgroups();

	@Override
	public final boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
		// The container platform can typically be detected from within a container by analyzing the cgroup file.
		// https://stackoverflow.com/a/52581380 was used as a reference
		try (Stream<String> stream = Files.lines(Paths.get("/proc/1/cgroup"))) {
			return stream.anyMatch(line -> getCgroups().stream().anyMatch(cGroup -> line.contains("/" + cGroup)));
		} catch (IOException e) {
			// We're probably just running on Windows here...
			return false;
		}
	}
}
