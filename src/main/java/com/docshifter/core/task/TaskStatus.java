package com.docshifter.core.task;

public enum TaskStatus {
	SUCCESS,
	FAILURE,
	BAD_INPUT,
	BAD_CONFIG;

	public boolean isSuccess() {
		return this == SUCCESS;
	}
}
