package com.docshifter.core.asposehelper.adapters;

import java.awt.*;
import java.io.InputStream;
import java.util.stream.Stream;

public interface UnifiedPage {
	Stream<RichTextParagraph> getHeaderText();
	Stream<RichTextParagraph> getFooterText();
	Stream<RichTextParagraph> getBodyText();
	Stream<InputStream> getImages();
	Color getBackgroundColor();
	int getNumber();
	//String getBookmarks();
}
