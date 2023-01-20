package com.docshifter.core.asposehelper.adapters;

public abstract class AbstractSegmentAdapter<T> extends AbstractAdapter<T> implements RichTextParagraph.Segment {
	protected AbstractSegmentAdapter(T adaptee) {
		super(adaptee);
	}

	@Override
	public String toString() {
		return getContent();
	}
}
