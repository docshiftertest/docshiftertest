package com.docshifter.core.operations;

import com.docshifter.core.config.domain.Node;
import java.nio.file.Path;
import java.util.Set;

/**
 * Created by michiel.vandriessche on 20/03/17.
 */
public class OptionParams extends OperationParams {


	private Set<Node> selectedNodes;

	public OptionParams(Path sourcePath) {
		super(sourcePath);
	}

	public OptionParams(OperationParams operationParams) {
		super(operationParams.getSourcePath(),
				operationParams.getResultPath(),
				operationParams.getParameters(),
				operationParams.isSuccess()
		);
	}

	public Set<Node> getSelectedNodes() {
		return selectedNodes;
	}

	public void setSelectedNodes(Set<Node> selectedNodes) {
		this.selectedNodes = selectedNodes;
	}
}
