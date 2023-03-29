package com.docshifter.core.config.entities;

/**
 * Rules that detail that a workflow can currently not be enabled, so it is in a partial state until resolved.
 */
public enum WorkflowRule {
	/**
	 * The workflow has no nodes at all.
	 */
	NO_NODES,
	/**
	 * There is at least one root node in the workflow that is not a sender node.
	 */
	NON_SENDER_ROOT,
	/**
	 * There is at least one leaf node in the workflow that is not a release node.
	 */
	NON_RELEASE_LEAF,
	/**
	 * One or more nodes are missing a module configuration.
	 */
	MISSING_MODULE_CONFIG,
	/**
	 * One or more nodes are disconnected from the main node graph/hierarchy. This means that there are effectively
	 * two or more separate workflows present.
	 */
	STRAY_NODES,
	/**
	 * An incompatible module configuration was assigned to an option module.
	 */
	INCOMPATIBLE_OPTION_MODULE_CONFIG,
	/**
	 * A sender node is not present at the start of the workflow. Having sender nodes branch from anywhere in the
	 * middle of the workflow is not supported (yet).
	 */
	SENDER_NODE_IN_MIDDLE
}
