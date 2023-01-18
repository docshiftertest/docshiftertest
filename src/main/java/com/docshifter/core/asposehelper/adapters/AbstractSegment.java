package com.docshifter.core.asposehelper.adapters;

public abstract class AbstractSegment implements RichTextParagraph.Segment {
	@Override
	public String toString() {
		return getContent();
	}
}
