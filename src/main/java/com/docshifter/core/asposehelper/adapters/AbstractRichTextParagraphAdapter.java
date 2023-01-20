package com.docshifter.core.asposehelper.adapters;

import java.util.stream.Collectors;

public abstract class AbstractRichTextParagraphAdapter<T> extends AbstractAdapter<T> implements RichTextParagraph {
	protected AbstractRichTextParagraphAdapter(T adaptee) {
		super(adaptee);
	}

	@Override
	public String toString() {
		return getSegments()
				.map(Segment::getContent)
				.collect(Collectors.joining());
	}
}
