package com.docshifter.core.task;

public enum TaskStatus {
	SUCCESS,
	FAILURE,
	BAD_INPUT,
	BAD_CONFIG,
	TIMED_OUT;

	public boolean isSuccess() {
		return this == SUCCESS;
	}
}
