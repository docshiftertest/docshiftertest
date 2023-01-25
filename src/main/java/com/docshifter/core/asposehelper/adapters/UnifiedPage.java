package com.docshifter.core.asposehelper.adapters;

import java.awt.*;
import java.io.InputStream;
import java.util.stream.Stream;

public interface UnifiedPage {
	Stream<RichTextParagraph> getHeaderText();
	Stream<RichTextParagraph> getFooterText();
	Stream<RichTextParagraph> getBodyText();
	Stream<Image> getImages();
	Stream<Bookmark> getBookmarks();
	Color getBackgroundColor();
	int getNumber();
	void markForDeletion();
}
