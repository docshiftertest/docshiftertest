package com.docshifter.core.asposehelper.utils.words;

import com.aspose.words.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over a {@link FieldToc} and extracts all the {@link FieldStart}s of its entries. Also supports entries to
 * be removed while iterating.
 */
public class TocIterator implements Iterator<FieldStart> {

	private static final Logger logger = LoggerFactory.getLogger(TocIterator.class);
	private final FieldStart startNode;
	private FieldStart currentNode;
	private Node nextNode;

	/**
	 * Constructs a new iterator instance.
	 * @param toc The Table of Contents field to iterate over.
	 */
	public TocIterator(FieldToc toc) {
		startNode = toc.getStart();
		currentNode = startNode;
	}

	@Override
	public boolean hasNext() {
		Node nextNode;
		if (currentNode == null || (nextNode = currentNode.nextPreOrder(startNode.getDocument())) == null || this.nextNode == nextNode) {
			return currentNode != this.nextNode;
		}

		this.nextNode = nextNode;
		return hasNextRecursive();
	}

	private boolean hasNextRecursive() {
		if (nextNode.getNodeType() == NodeType.FIELD_START) {
			int nextFieldType = ((FieldStart) nextNode).getFieldType();
			logger.trace("It's a FIELD_START for " + FieldType.getName(nextFieldType));
			if (nextFieldType == FieldType.FIELD_HYPERLINK) {
				logger.trace("It's a FIELD_HYPERLINK!");
				return true;
			}
		} else if (nextNode.getNodeType() == NodeType.FIELD_END) {
			int nextFieldType = ((FieldEnd) nextNode).getFieldType();
			logger.trace("It's a FIELD_END for " + FieldType.getName(nextFieldType));
			if (nextFieldType == FieldType.FIELD_TOC) {
				logger.trace("It's a FIELD_TOC end, returning FALSE for hasNext!");
				nextNode = currentNode;
				return false;
			}
		}

		nextNode = nextNode.nextPreOrder(startNode.getDocument());
		return hasNextRecursive();
	}

	@Override
	public void remove() {
		if (currentNode == null || currentNode == startNode) {
			throw new IllegalStateException("Node has already been removed or next has not been called yet.");
		}

		boolean hasNext = hasNext();
		Paragraph para = currentNode.getParentParagraph();
		if (para == startNode.getParentParagraph()) {
			// If we're removing the first entry of a ToC, we just want to remove the hyperlink field, otherwise the
			// entire ToC field will be messed up if we remove the parent paragraph because that field will also be
			// present in the first paragraph. Then we want to copy the remaining nodes in the paragraph to the new
			// first entry and we can safely remove the old paragraph.
			try {
				currentNode.getField().remove();

				if (hasNext) {
					Paragraph newPara = ((Paragraph) para.getNextSibling());
					Node origFirstChild = newPara.getFirstChild();
					for (Node child : para.getChildNodes(NodeType.ANY, false).toArray()) {
						newPara.insertBefore(child, origFirstChild);
					}
					para.remove();
				} else {
					Run run = new Run(para.getDocument(), "No table entries found.");
					run.getFont().setBold(true);
					para.appendChild(run);
				}
			} catch (Exception ex) {
				logger.error("Could not remove first entry of TOC.", ex);
			}
		} else {
			// For entries below the first entry, we can simply remove the parent paragraph and it'll get rid of the
			// field and line.
			currentNode.getField().getStart().getParentParagraph().remove();
		}

		currentNode = null;
		if (!hasNext) {
			nextNode = null;
		}
	}

	@Override
	public FieldStart next() {
		if (!hasNext()) {
			throw new NoSuchElementException("There is no next node.");
		}

		currentNode = (FieldStart) nextNode;
		nextNode = null;
		return currentNode;
	}

	/**
	 * Utility method to extract text from a TOC hyperlink node.
	 * @param node The node associated with the TOC entry.
	 * @return The text contained within the node.
	 */
	public static String extractEntryText(FieldStart node) {
		Node now = null;
		for (Node currNode = node; currNode != null; currNode = currNode.getNextSibling()) {
			if (currNode.getNodeType() == NodeType.FIELD_SEPARATOR) {
				logger.trace("It's a FIELD_SEPARATOR!");
				now = currNode;
				break;
			}
		}

		if (now == null) {
			throw new IllegalArgumentException("Could not get to the separator field of the node: is the provided " +
					"node associated with a hyperlink field?");
		}

		StringBuilder sBuf = new StringBuilder();
		now = now.getNextSibling();
		// The title may be split into several RUNs
		while (now != null && now.getNextSibling() != null
				&& now.getNodeType() == NodeType.RUN) {
			sBuf.append(now.getText());
			now = now.getNextSibling();
		}
		return sBuf.toString();
	}
}
