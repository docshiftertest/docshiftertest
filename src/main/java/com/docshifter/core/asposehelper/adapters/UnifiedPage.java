package com.docshifter.core.asposehelper.adapters;

import java.awt.*;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A general page inside a {@link UnifiedDocument}. Allows access to all of its {@link PageResource}s.
 */
public interface UnifiedPage {
	/**
	 * Represents a distinct part of the page.
	 */
	interface PageSection {
		/**
		 * Gets all text present inn the page section, grouped by paragraph.
		 */
		Stream<RichTextParagraph> getTextParagraphs();

		/**
		 * Gets all images present in the page section.
		 */
		Stream<Image> getImages();

		/**
		 * Gets all bookmarks in the page section.
		 */
		Stream<Bookmark> getBookmarks();

		/**
		 * Mark the page section for deletion. Call {@link UnifiedDocument#commitDeletes()} to perform the actual
		 * delete operations.
		 */
		void markForDeletion();
	}
	/**
	 * Gets the header section of the page (if any).
	 */
	Optional<PageSection> getHeader();

	/**
	 * Gets the footer section of the page (if any).
	 */
	Optional<PageSection> getFooter();

	/**
	 * Gets the main/body section of the page.
	 */
	PageSection getBody();

	/**
	 * Gets the background color of the page.
	 */
	Color getBackgroundColor();

	/**
	 * Gets the 1-based index number of the page.
	 */
	int getNumber();

	/**
	 * Mark the page for deletion. Call {@link UnifiedDocument#commitDeletes()} to perform the actual delete operations.
	 */
	void markForDeletion();
}
