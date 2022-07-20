package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.OutlineItemCollection;
import com.aspose.pdf.Outlines;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FlatOutlineIterator implements Iterator<OutlineItemCollection> {
	private final Outlines root;
	private OutlineItemCollection currItem;

	public FlatOutlineIterator(Outlines outlines) {
		root = outlines;
	}

	public static Iterable<OutlineItemCollection> createIterable(Outlines outlines) {
		return () -> new FlatOutlineIterator(outlines);
	}

	@Override
	public boolean hasNext() {
		if (currItem == null) {
			return root.size() > 0;
		}

		return hasNext(currItem, true);
	}

	private boolean hasNext(OutlineItemCollection currItem, boolean considerChildren) {
		if (considerChildren && currItem.size() > 0) {
			return true;
		}

		if (currItem.hasNext()) {
			return true;
		}

		Outlines parent = currItem.getParent();
		if (parent instanceof OutlineItemCollection oic) {
			return hasNext(oic, false);
		}

		return false;
	}

	@Override
	public OutlineItemCollection next() {
		return next(true);
	}

	private OutlineItemCollection next(boolean considerChildren) {
		if (currItem == null) {
			for (OutlineItemCollection oic : root) {
				currItem = oic;
				break;
			}
		} else if (considerChildren && currItem.size() > 0) {
			for (OutlineItemCollection oic : currItem) {
				currItem = oic;
				break;
			}
		} else if (currItem.hasNext()) {
			currItem = currItem.next();
		} else {
			Outlines parent = currItem.getParent();
			if (parent instanceof OutlineItemCollection oic) {
				currItem = oic;
				return next(false);
			}
		}

		if (currItem == null) {
			throw new NoSuchElementException();
		}
		return currItem;
	}

	@Override
	public void remove() {
		currItem.getParent().remove(currItem);
	}
}
