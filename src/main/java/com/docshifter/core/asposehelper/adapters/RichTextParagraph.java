package com.docshifter.core.asposehelper.adapters;

import java.awt.*;
import java.util.stream.Stream;

public interface RichTextParagraph {
	//boolean isBookmarkPointingHere();
	Alignment getHorizontalAlignment();
	Stream<Segment> getSegments();

	enum Alignment {
		NONE,
		LEFT,
		CENTER,
		RIGHT,
		JUSTIFIED,
		FULLY_JUSTIFIED,
		ARABIC_LOW_KISHIDA,
		ARABIC_MEDIUM_KISHIDA,
		ARABIC_HIGH_KISHIDA,
		MATH_ELEMENT_CENTER_AS_GROUP,
		THAI_DISTRIBUTED
	}

	interface Segment {
		String getContent();
		boolean isBold();
		boolean isItalic();
		boolean isUnderline();
		boolean isStrikethrough();
		boolean isInvisibleRendering();
		Color getForegroundColor();
		Color getBackgroundColor();
		Color getUnderlineColor();
		double getFontSize();
		String getFontName();
	}
}
