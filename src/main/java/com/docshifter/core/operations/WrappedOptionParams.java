package com.docshifter.core.operations;

import com.docshifter.core.config.entities.Node;
import com.docshifter.core.task.TaskStatus;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link OptionParams} that wraps one or more other {@link OptionParams} and aggregates their results. Single
 * params can also be queried individually.
 */
public class WrappedOptionParams extends OptionParams implements OperationsWrapper<OptionParams> {
	private final OperationsWrapper<OptionParams> wrapper = new WrappedParams<>();

	public WrappedOptionParams(Path sourcePath) {
		super(sourcePath);
	}

	public WrappedOptionParams(OperationParams operationParams) {
		super(operationParams);
	}

	@Override
	public Object clone() {
		return new WrappedOptionParams(this);
	}

	@Override
	public void wrap(OptionParams param) {
		wrapper.wrap(param);
	}

	@Override
	public Set<OptionParams> getWrapped() {
		return wrapper.getWrapped();
	}

	@Override
	public Set<OptionParams> getWrappedFlattened() {
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

	/**
	 * Aggregates all the selected nodes in the wrapped {@link OptionParams}.
	 * @return
	 */
	@Override
	public Map<Path, Set<Node>> getSelectedNodes() {
		return getWrappedFlattened().stream()
				.map(OptionParams::getSelectedNodes)
				.reduce((first, second) -> {
					first.putAll(second);
					return first;
				})
				.orElse(new HashMap<>());
	}

	/**
	 * Do not call this, it will throw an exception.
	 * @param selectedNodes
	 */
	@Override
	public void setSelectedNodes(Map<Path, Set<Node>> selectedNodes) {
		throw new UnsupportedOperationException("Cannot set selected nodes on a wrapper.");
	}
}
