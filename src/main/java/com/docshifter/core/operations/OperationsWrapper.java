package com.docshifter.core.operations;

import com.docshifter.core.task.TaskStatus;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An interface marking a class wrapping classes derived from {@link OperationParams}. Each wrapped instance can be
 * queried individually, or the results of all wrapped instances can be combined.
 * @param <T>
 */
interface OperationsWrapper<T extends OperationParams> {
	/**
	 * Wrap/add a new param.
	 * @param param The param to add. Its results will be aggreggated.
	 */
	void wrap(T param);

	/**
	 * Get all wrapped params.
	 * @return The individual wrapped params.
	 */
	Set<T> getWrapped();

	/**
	 * Get all wrapped params, traveling down as deep as possible in case of any nested wrappers.
	 * @return The inidividual wrapped params in a completely flattened hierarchy.
	 */
	Stream<T> getWrappedFlattened();

	/**
	 * If there are any successful {@link TaskStatus}es present in the wrapped params, returns that, otherwise will
	 * return the most severe {@link TaskStatus}.
	 * @return
	 */
	TaskStatus getSuccess();

	/**
	 * Checks if there are any success {@link TaskStatus}es present in the wrapped params.
	 * @return
	 */
	boolean isSuccess();

	/**
	 * Do not call this, it should throw an exception.
	 * @param success
	 */
	void setSuccess(TaskStatus success);

	/**
	 * Get all aggreggated parameters in the wrapped params.
	 * @return
	 */
	Map<String, Object> getParameters();

	/**
	 * Find a single parameter in all the wrapped params.
	 * @param name The name of the parameter to find.
	 * @return The parameter, <code>null</code> otherwise.
	 */
	Object getParameter(String name);
	/**
	 * Do not call this, it should throw an exception.
	 * @param name
	 * @param o
	 */
	void addParameter(String name, Object o);
	/**
	 * Do not call this, it should throw an exception.
	 * @param parameters
	 */
	void setParameters(Map parameters);
}
