package com.docshifter.core.operations;

/**
 * When processing directory input, indicates how a failure of a single file should be handled.
 */
public enum FailureLevel {
	/**
	 * Failure happens on file level: if a file fails, just ignore it and continue processing the rest. This is the
	 * lowest level.
	 */
	FILE,
	/**
	 * Failure happens on group level: if a file fails, fail the file and all the files in the group it is part of.
	 */
	GROUP,
	/**
	 * Failure happens on branch level: if a file fails, fail all the groups and files that are part of this branch.
	 * Any other active branches may continue processing.
	 */
	BRANCH,
	/**
	 * Failure happens on transformation level: if a file fails, abort the entire transformation. This is the highest
	 * level.
	 */
	TRANSFORMATION;

	/**
	 * Whether the provided {@link FailureLevel} is higher than the current one.
	 * @param other The {@link FailureLevel} to compare to.
	 * @return true if higher, false if equal or lower.
	 */
	public boolean isHigherThan(FailureLevel other) {
		return ordinal() > other.ordinal();
	}

	/**
	 * Whether the provided {@link FailureLevel} is lower than the current one.
	 * @param other The {@link FailureLevel} to compare to.
	 * @return true if lower, false if equal or higher.
	 */
	public boolean isLowerThan(FailureLevel other) {
		return ordinal() < other.ordinal();
	}
}
