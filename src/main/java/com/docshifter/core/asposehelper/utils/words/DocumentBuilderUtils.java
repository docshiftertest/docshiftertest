package com.docshifter.core.asposehelper.utils.words;

import com.aspose.words.DocumentBuilder;
import com.aspose.words.Node;

public final class DocumentBuilderUtils {
	private DocumentBuilderUtils() {}

	/**
	 * Retrieves the most precise position from a {@link DocumentBuilder}. This is either the current node or the
	 * current paragraph.
	 * @param docBuilder The {@link DocumentBuilder}.
	 * @return The node corresponding with the current position in the document.
	 */
	public static Node getCurrentPosition(DocumentBuilder docBuilder) {
		Node currNode = docBuilder.getCurrentNode();
		return currNode == null ? docBuilder.getCurrentParagraph() : currNode;
	}

	/**
	 * Moves a {@link DocumentBuilder} to the paragraph above the current paragraph, if it exists.
	 * @param docBuilder The {@link DocumentBuilder}.
	 */
	public static void moveToPreviousParagraph(DocumentBuilder docBuilder) {
		Node prev = docBuilder.getCurrentParagraph().getPreviousSibling();
		if (prev != null) {
			docBuilder.moveTo(prev);
		}
	}

	/**
	 * Inserts an empty line at the current position and stays on it.
	 * @param docBuilder The {@link DocumentBuilder}.
	 */
	public static void writelnNoJump(DocumentBuilder docBuilder) {
		docBuilder.writeln();
		moveToPreviousParagraph(docBuilder);
	}

	/**
	 * Inserts some text on a new line at the current position and stays on it.
	 * @param docBuilder The {@link DocumentBuilder}.
	 * @param text The text to insert.
	 */
	public static void writelnNoJump(DocumentBuilder docBuilder, String text) {
		docBuilder.writeln(text);
		moveToPreviousParagraph(docBuilder);
	}
}
