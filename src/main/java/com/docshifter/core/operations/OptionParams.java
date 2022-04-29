package com.docshifter.core.operations;

import com.docshifter.core.config.entities.Node;

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

	private OptionParams(OperationParams operationParams) {
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

	public static OptionParams fromOperationParams(OperationParams operationParams) {
		return new OptionParams(operationParams);
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
}
