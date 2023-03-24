package com.docshifter.core.asposehelper.adapters;

import java.awt.*;
import java.util.stream.Stream;

public interface RichTextParagraph extends PageResource {
	Alignment getHorizontalAlignment();
	void setHorizontalAlignment(Alignment alignment);
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

	interface Segment extends Child<RichTextParagraph> {
		String getContent();
		void setContent(String content);
		boolean isBold();
		void setBold(boolean bold);
		boolean isItalic();
		void setItalic(boolean italic);
		boolean isUnderline();
		void setUnderline(boolean underline);
		boolean isStrikethrough();
		void setStrikethrough(boolean strikethrough);
		boolean isInvisibleRendering();
		boolean isInvisibleRenderingSupported();
		void setInvisibleRendering(boolean invisibleRendering);
		Color getForegroundColor();
		void setForegroundColor(Color color);
		Color getBackgroundColor();
		void setBackgroundColor(Color color);
		Color getUnderlineColor();
		void setUnderlineColor(Color color);
		double getFontSize();
		void setFontSize(double fontSize);
		String getFontName();
		void setFontName(String fontName);
		Segment splitAt(int index);
		void markForDeletion();
	}
}
