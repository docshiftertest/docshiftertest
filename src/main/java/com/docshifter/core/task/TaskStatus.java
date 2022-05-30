package com.docshifter.core.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum TaskStatus {
	SUCCESS(0),
	FAILURE(4),
	BAD_INPUT(2),
	BAD_CONFIG(3),
	TIMED_OUT(1);

	private static final int MAX_SEVERITY = Arrays.stream(values())
			.mapToInt(TaskStatus::getSeverity)
			.max()
			.orElseThrow(() -> new IllegalStateException("Could not find any enum values, this should never happen."));

	private final int severity;

	public boolean isWorseThan(TaskStatus other) {
		return severity > other.severity;
	}

	public TaskStatus getWorse(TaskStatus other) {
		if (other.isWorseThan(this)) {
			return other;
		}
		return this;
	}

	public boolean isWorst() {
		return severity == MAX_SEVERITY;
	}

	public boolean isSuccess() {
		return this == SUCCESS;
	}
}
