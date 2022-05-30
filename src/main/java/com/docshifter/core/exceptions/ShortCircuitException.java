package com.docshifter.core.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Utility {@link RuntimeException} to throw from within a {@link java.util.stream.Stream} in order to interrupt it.
 * Can contain a resulting data object that you can pass from within the stream.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ShortCircuitException extends RuntimeException {
	private final Object data;

	public ShortCircuitException() {
		this(null);
	}

	public ShortCircuitException(Object data) {
		super();
		this.data = data;
	}
}
