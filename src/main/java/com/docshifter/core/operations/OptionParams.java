package com.docshifter.core.operations;

import com.docshifter.core.config.entities.Node;
import com.docshifter.core.task.TaskStatus;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Created by michiel.vandriessche on 20/03/17.
 */
public class OptionParams extends OperationParams {


	private Map<Path, Set<Node>> selectedNodes;

	public OptionParams(Path sourcePath) {
		super(sourcePath);
	}

	public OptionParams(OperationParams operationParams) {
		super(operationParams.getSourcePath(),
				operationParams.getResultPath(),
				operationParams.getParameters(),
				operationParams.getSuccess()
		);
	}

	public OptionParams(OptionParams optionParams) {
		this((OperationParams) optionParams);
		selectedNodes = optionParams.selectedNodes;
	}

	public Map<Path, Set<Node>> getSelectedNodes() {
		return selectedNodes;
	}

	public void setSelectedNodes(Map<Path, Set<Node>> selectedNodes) {
		this.selectedNodes = selectedNodes;
	}

	@Override
	public Object clone() {
		return new OptionParams(this);
	}

	@Override
	public OperationParams merge(TaskStatus other) {
		OptionParams cloned = new OptionParams(this);
		if (isSuccess() && !other.isSuccess()) {
			cloned.setSuccess(other);
		}
		return cloned;
	}

	@Override
	public OperationParams merge(OperationParams other) {
		if (!(other instanceof OptionParams)) {
			throw new IllegalArgumentException("Other instance to merge must derive from OptionParams");
		}
		OptionParams cloned = new OptionParams(this);
		if (isSuccess() && !other.isSuccess()) {
			cloned.setSuccess(other.getSuccess());
		}
		cloned.getParameters().putAll(other.getParameters());
		cloned.selectedNodes.putAll(((OptionParams)other).selectedNodes);
		return cloned;
	}
}
