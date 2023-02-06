package com.docshifter.core.config.conditions;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IsInContainerCondition implements Condition {
	@Override
	public boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
		try {
			// Scan for empty marker file that was created as part of the Docker build
			return Files.exists(Paths.get("/a602a2cd-ef1c-4c95-a32c-af8a10cc51cf"));
		} catch (Exception ex) {
			// Any exception automatically means no...
			return false;
		}
	}
}
