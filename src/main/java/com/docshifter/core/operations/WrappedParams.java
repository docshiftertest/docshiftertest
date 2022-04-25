package com.docshifter.core.operations;

import com.docshifter.core.task.TaskStatus;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class WrappedParams<T extends OperationParams> implements OperationsWrapper<T> {
	private final Set<T> params = ConcurrentHashMap.newKeySet();

	@Override
	public void wrap(T param) {
		params.add(param);
	}

	@Override
	public Set<T> getWrapped() {
		return Collections.unmodifiableSet(params);
	}

	@Override
	public Set<T> getWrappedFlattened() {
		return Collections.unmodifiableSet(params.stream()
				.flatMap(param -> {
					if (param instanceof OperationsWrapper) {
						return ((OperationsWrapper<T>) param).getWrappedFlattened().stream();
					}
					return Stream.of(param);
				})
				.collect(Collectors.toSet()));
	}

	@Override
	public TaskStatus getSuccess() {
		return params.stream()
				.map(OperationParams::getSuccess)
				.filter(status -> !status.isSuccess())
				.findFirst()
				.orElse(TaskStatus.SUCCESS);
	}

	@Override
	public boolean isSuccess() {
		return getSuccess().isSuccess();
	}

	@Override
	public void setSuccess(TaskStatus success) {
		throw new UnsupportedOperationException("Cannot set success status on a wrapper.");
	}
}
