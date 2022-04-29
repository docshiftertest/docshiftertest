package com.docshifter.core.operations;

import com.docshifter.core.config.entities.Node;
import com.docshifter.core.task.TaskStatus;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * {@link OptionParams} that wraps one or more other {@link OptionParams} and aggregates their results. Single
 * params can also be queried individually.
 */
public class WrappedOptionParams extends OptionParams implements OperationsWrapper<OptionParams> {
	private final OperationsWrapper<OptionParams> wrapper = new WrappedParams<>();

	public WrappedOptionParams(Path sourcePath) {
		super(sourcePath);
	}

	private WrappedOptionParams(OptionParams optionParams) {
		super(optionParams);
	}

	public static WrappedOptionParams fromOptionParams(OptionParams optionParams) {
		return new WrappedOptionParams(optionParams);
	}

	// We shouldn't have a WrappedOperationParams(WrappedOperationParams wrappedOperationParams) nor clone because for
	// subsequent operations we don't want the old wrapped OperationParams to be cloned, we usually want a fresh start!

	@Override
	public void wrap(OptionParams param) {
		wrapper.wrap(param);
	}

	@Override
	public Set<OptionParams> getWrapped() {
		return wrapper.getWrapped();
	}

	@Override
	public Stream<OptionParams> getWrappedFlattened() {
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
		return getWrappedFlattened()
				.filter(OptionParams::isSuccess)
				.map(OptionParams::getSelectedNodes)
				.reduce((first, second) -> {
					first.putAll(second);
					return first;
				})
				.orElseThrow(() -> new IllegalStateException("No params have been wrapped yet, so cannot combine " +
						"selected nodes"));
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
