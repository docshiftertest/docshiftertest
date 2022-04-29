package com.docshifter.core.operations;

import com.docshifter.core.task.TaskStatus;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * {@link OperationParams} that wraps one or more other {@link OperationParams} and aggregates their results. Single
 * params can also be queried individually.
 */
public class WrappedOperationParams extends OperationParams implements OperationsWrapper<OperationParams> {
	private final OperationsWrapper<OperationParams> wrapper = new WrappedParams<>();

	public WrappedOperationParams(Path sourcePath) {
		super(sourcePath);
	}

	private WrappedOperationParams(OperationParams operationParams) {
		super(operationParams);
	}

	public static WrappedOperationParams fromOperationParams(OperationParams operationParams) {
		return new WrappedOperationParams(operationParams);
	}

	// We shouldn't have a WrappedOperationParams(WrappedOperationParams wrappedOperationParams) nor clone because for
	// subsequent operations we don't want the old wrapped OperationParams to be cloned, we usually want a fresh start!

	@Override
	public void wrap(OperationParams param) {
		wrapper.wrap(param);
	}

	@Override
	public Set<OperationParams> getWrapped() {
		return wrapper.getWrapped();
	}

	@Override
	public Stream<OperationParams> getWrappedFlattened() {
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

	@Override
	public Map<String, Object> getParameters() {
		return wrapper.getParameters();
	}

	@Override
	public Object getParameter(String name) {
		return wrapper.getParameter(name);
	}

	@Override
	public void addParameter(String name, Object o) {
		wrapper.addParameter(name, o);
	}

	@Override
	public void setParameters(Map parameters) {
		wrapper.setParameters(parameters);
	}
}
