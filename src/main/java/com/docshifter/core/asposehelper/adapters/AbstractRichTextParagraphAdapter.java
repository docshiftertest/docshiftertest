package com.docshifter.core.asposehelper.adapters;

import java.util.stream.Collectors;

public abstract class AbstractRichTextParagraphAdapter<T> extends AbstractAdapterChild<T, UnifiedPage.PageSection> implements RichTextParagraph {
	protected AbstractRichTextParagraphAdapter(T adaptee, UnifiedPage.PageSection parent) {
		super(adaptee, parent);
	}

	@Override
	public Type getType() {
		return Type.TEXT_PARAGRAPH;
	}

	@Override
	public String toString() {
		return getSegments()
				.map(Segment::getContent)
				.collect(Collectors.joining());
	}
}
