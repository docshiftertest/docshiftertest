package com.docshifter.core.operations;

import com.docshifter.core.task.TaskStatus;

import java.util.Set;

interface OperationsWrapper<T extends OperationParams> {
	void wrap(T param);
	Set<T> getWrapped();
	Set<T> getWrappedFlattened();
	TaskStatus getSuccess();
	boolean isSuccess();
	void setSuccess(TaskStatus success);
}
