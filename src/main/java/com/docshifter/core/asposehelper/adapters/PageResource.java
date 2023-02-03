package com.docshifter.core.asposehelper.adapters;

/**
 * Marker interface that describes any resource present on a {@link UnifiedPage}.
 */
public interface PageResource extends Child<UnifiedPage.PageSection> {
	void markForDeletion();
	Type getType();
	enum Type {
		TEXT_PARAGRAPH,
		IMAGE,
		BOOKMARK
	}
}
