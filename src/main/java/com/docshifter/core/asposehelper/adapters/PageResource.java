package com.docshifter.core.asposehelper.adapters;

public interface PageResource {
	void markForDeletion();
	Type getType();
	enum Type {
		TEXT_PARAGRAPH,
		IMAGE,
		BOOKMARK
	}
}
