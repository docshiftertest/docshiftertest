package com.docshifter.core.operations;

/**
 * Specifies how directory input should be processed in operations.
 */
public enum DirectoryHandling {
	/**
	 * Indicates that a module wants to receive directories in their original form, meaning that it is willing to
	 * accept and handle both single files and directories in a custom manner (instead of simply looping over the
	 * directory hierarchy and processing files one by one).
	 */
	AS_IS,
	/**
	 * Indicates that a module wants input directories to be iterated over, and that it only expects to process single
	 * files as a result. It does not have a notion of directory input and prefers the iteration over the directory to
	 * automatically take place in a sequential fashion.
	 */
	SEQUENTIAL_FOREACH,
	/**
	 * Indicates that a module wants input directories to be iterated over, and that it only expects to process single
	 * files as a result. It does not have a notion of directory input and prefers the iteration over the directory to
	 * automatically take place in a parallel fashion.
	 */
	PARALLEL_FOREACH
}
