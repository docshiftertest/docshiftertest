package com.docshifter.core.operations;

import com.docshifter.core.exceptions.ShortCircuitException;
import com.docshifter.core.task.TaskStatus;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
	public Stream<T> getWrappedFlattened() {
		return params.stream()
				.flatMap(param -> {
					if (param instanceof OperationsWrapper) {
						return ((OperationsWrapper<T>) param).getWrappedFlattened();
					}
					return Stream.of(param);
				});
	}

	@Override
	public TaskStatus getSuccess() {
		try {
			return getWrappedFlattened()
					.map(OperationParams::getSuccess)
					.reduce(this::getWorseOrShortCircuit)
					.orElseThrow(() -> new IllegalStateException("No params have been wrapped yet, so cannot " +
							"determine success status."));
		} catch (ShortCircuitException ex) {
			return (TaskStatus) ex.getData();
		}
	}

	private TaskStatus getWorseOrShortCircuit(TaskStatus first, TaskStatus second) {
		if (first.isSuccess() || first.isWorst()) {
			throw new ShortCircuitException(first);
		}
		if (second.isSuccess()) {
			return second;
		}
		return first.getWorse(second);
	}

	@Override
	public boolean isSuccess() {
		Iterator<T> it = getWrappedFlattened().iterator();
		if (!it.hasNext()) {
			throw new IllegalStateException("No params have been wrapped yet, so cannot " +
					"determine success status.");
		}
		while (it.hasNext()) {
			if (it.next().isSuccess()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setSuccess(TaskStatus success) {
		throw new UnsupportedOperationException("Cannot set success status on a wrapper.");
	}

	@Override
	public Map<String, Object> getParameters() {
		return getWrappedFlattened()
				.map(OperationParams::getParameters)
				.reduce((first, second) -> {
					first.putAll(second);
					return first;
				})
				.orElseThrow(() -> new IllegalStateException("No params have been wrapped yet, so cannot combine " +
						"parameters"));
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
