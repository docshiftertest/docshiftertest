package com.docshifter.core.utils;

/**
 * Common keys to be added in task data and used across docshifter.
 * @author Juan Marques created on 08/10/2020
 *
 */
public enum TaskDataKey {

	DOCUMENT_BOUNDARY_TEXT,
	ADD_BOUNDARY_TITLE,
	PROCESS_DIRECTORY("mergeFiles"); // TODO: deprecate mergeFiles and make naming consistent with other keys (MERGE_FILES)?

	private final String alias;

	TaskDataKey(String alias) {
		this.alias = alias;
	}

	TaskDataKey() {
		alias = null;
	}

	@Override
	public String toString() {
		if (alias == null) {
			return name();
		}
		return alias;
	}
}
