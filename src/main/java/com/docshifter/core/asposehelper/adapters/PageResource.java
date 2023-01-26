package com.docshifter.core.asposehelper.adapters;

/**
 * Marker interface that describes any resource present on a {@link UnifiedPage}.
 */
public interface PageResource {
	void markForDeletion();
	Type getType();
	enum Type {
		TEXT_PARAGRAPH,
		IMAGE,
		BOOKMARK
	}
}
