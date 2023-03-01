package com.docshifter.core.asposehelper.adapters;

public interface Bookmark extends PageResource {
	String getTitle();
	boolean pointsTo(RichTextParagraph paragraph);
	boolean supportsStyling();
	boolean isBold();
	void setBold(boolean bold);
	boolean isItalic();
	void setItalic(boolean italic);
}
