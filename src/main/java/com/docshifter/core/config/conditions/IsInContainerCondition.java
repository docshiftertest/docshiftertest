package com.docshifter.core.config.conditions;

import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.nio.file.Files;
import java.nio.file.Paths;

public class IsInContainerCondition extends AnyNestedCondition {
	public IsInContainerCondition() {
		super(ConfigurationPhase.REGISTER_BEAN);
	}

	@Conditional(MarkerFileExistsCondition.class)
	static class MarkerFileExists {}

	// Normally the MarkerFileExists condition should always match, even when in Kubernetes. However, if somehow it
	// doesn't, then it likely means someone is playing dirty by force deleting the container marker file to pretend
	// it's a classical installation, perhaps in an attempt to work around the max receivers check! In that case, we
	// can still detect a container environment by scanning for possible Kubernetes clues...
	@Conditional(IsInKubernetesCondition.class)
	static class IsInKubernetes {}

	private static class MarkerFileExistsCondition implements Condition {
		@Override
		public boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
			try {
				// Scan for empty marker file that was created as part of the Docker build
				return Files.exists(Paths.get("/.a602a2cd-ef1c-4c95-a32c-af8a10cc51cf"));
			} catch (Exception ex) {
				// Any exception automatically means no...
				return false;
			}
		}
	}
}
