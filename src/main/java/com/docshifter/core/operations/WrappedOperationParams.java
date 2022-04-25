package com.docshifter.core.operations;

import com.docshifter.core.task.TaskStatus;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class WrappedOperationParams extends OperationParams implements OperationsWrapper<OperationParams> {
	private final OperationsWrapper<OperationParams> wrapper = new WrappedParams<>();

	public WrappedOperationParams(Path sourcePath) {
		super(sourcePath);
	}

	public WrappedOperationParams(Path sourcePath, Path resultPath, Map<String, Object> parameters, TaskStatus success) {
		super(sourcePath, resultPath, parameters, success);
	}

	public WrappedOperationParams(OperationParams operationParams) {
		super(operationParams);
	}

	@Override
	public Object clone() {
		return new WrappedOperationParams(this);
	}

	@Override
	public void wrap(OperationParams param) {
		wrapper.wrap(param);
	}

	@Override
	public Set<OperationParams> getWrapped() {
		return wrapper.getWrapped();
	}

	@Override
	public Set<OperationParams> getWrappedFlattened() {
		return wrapper.getWrappedFlattened();
	}

	@Override
	public TaskStatus getSuccess() {
		return wrapper.getSuccess();
	}

	@Override
	public boolean isSuccess() {
		return wrapper.isSuccess();
	}

	@Override
	public void setSuccess(TaskStatus success) {
		wrapper.setSuccess(success);
	}
}
