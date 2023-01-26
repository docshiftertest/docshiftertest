package com.docshifter.core.asposehelper.adapters;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A general interface that allows iterating over and performing modifications on the structure of a document that is
 * divided in pages.
 */
public interface UnifiedDocument extends Closeable {
	/**
	 * Gets all pages in the document.
	 */
	Stream<UnifiedPage> getPages();

	/**
	 * Commits all previously marked delete operations in the document tree.
	 */
	void commitDeletes();

	/**
	 * Persists all changes made to the document to a file at the specified path.
	 * @param path The file path to save the modified document to.
	 */
	void save(String path);
	/**
	 * Persists all changes made to the document to a file at the specified path.
	 * @param path The file path to save the modified document to.
	 */
	default void save(Path path) {
		save(path.toString());
	}
}
