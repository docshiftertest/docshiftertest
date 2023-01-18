package com.docshifter.core.asposehelper.adapters;

import java.util.stream.Collectors;

public abstract class AbstractRichTextParagraph implements RichTextParagraph {
	@Override
	public String toString() {
		return getSegments()
				.map(Segment::getContent)
				.collect(Collectors.joining());
	}
}
