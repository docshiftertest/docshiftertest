package com.docshifter.core.operations;

import com.docshifter.core.task.TaskStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class to aid in wrapping classes deriving from {@link OperationParams} and to reduce unnecessary duplication.
 * @param <T>
 */
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
		return getWrappedFlattened().stream()
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

	@Override
	public Map<String, Object> getParameters() {
		return getWrappedFlattened().stream()
				.map(OperationParams::getParameters)
				.reduce((first, second) -> {
					first.putAll(second);
					return first;
				})
				.orElse(new HashMap<>());
	}

	@Override
	public Object getParameter(String name) {
		return getParameters().get(name);
	}

	@Override
	public void addParameter(String name, Object o) {
		throw new UnsupportedOperationException("Cannot add parameter on a wrapper.");
	}

	@Override
	public void setParameters(Map parameters) {
		throw new UnsupportedOperationException("Cannot set parameters on a wrapper.");
	}
}
