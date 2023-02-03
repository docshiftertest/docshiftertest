package com.docshifter.core.asposehelper.adapters;

public abstract class AbstractSegmentAdapter<T> extends AbstractAdapterChild<T, RichTextParagraph> implements RichTextParagraph.Segment {
	protected AbstractSegmentAdapter(T adaptee, RichTextParagraph parent) {
		super(adaptee, parent);
	}

	@Override
	public RichTextParagraph.Segment splitAt(int index) {
		if (index == 0) {
			throw new IllegalArgumentException("It makes no sense to split at the beginning!");
		}
		if (index >= getContent().length()) {
			throw new IllegalArgumentException("Cannot split on an index equal to or greater than the length of the " +
					"segment!");
		}
		String end = getContent().substring(index);
		setContent(getContent().substring(0, index));
		return doSplit(end);
	}

	protected abstract RichTextParagraph.Segment doSplit(String end);

	@Override
	public String toString() {
		return getContent();
	}
}
