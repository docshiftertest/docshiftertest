package com.docshifter.core.asposehelper.adapters;

public abstract class AbstractAdapterChild<TAdaptee, TParent> extends AbstractAdapter<TAdaptee> {
	protected final TParent parent;

	protected AbstractAdapterChild(TAdaptee adaptee, TParent parent) {
		super(adaptee);
		this.parent = parent;
	}

	public TParent getParent() {
		return parent;
	}
}
