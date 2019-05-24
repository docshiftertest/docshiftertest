package com.docshifter.core.config.domain;

public enum QueueMonitorState {
	QUEUED,
	PROCESSING,
	CANCEL,
	IGNORED,
	FINISHED;

	public String toString() {
		return name().charAt(0) + name().substring(1).toLowerCase();
	}
}
