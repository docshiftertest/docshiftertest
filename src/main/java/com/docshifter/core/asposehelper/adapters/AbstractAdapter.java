package com.docshifter.core.asposehelper.adapters;

public abstract class AbstractAdapter<T> {
	protected final T adaptee;

	protected AbstractAdapter(T adaptee) {
		this.adaptee = adaptee;
	}

	public T getAdaptee() {
		return adaptee;
	}
}
