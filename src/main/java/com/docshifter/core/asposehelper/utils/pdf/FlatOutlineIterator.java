package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.OutlineItemCollection;
import com.aspose.pdf.Outlines;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over all the bookmark/outlines present in an entire hierarchy in a logical, flat order. Logical order means
 * the vertical order you would encounter in Adobe Acrobat (so from top to bottom) when you expand all the bookmarks.
 */
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
		// Before calling our first next(), check if root is an oic (and not an OutlineCollection) itself or if there
		// are any bookmarks available from root first.
		if (currItem == null) {
			return root instanceof OutlineItemCollection || root.size() > 0;
		}

		return hasNext(currItem, true);
	}

	/**
	 * Recursive call that first checks if the current bookmark has any children to traverse into next, then if it
	 * has any siblings, and finally if there is a next bookmark to visit somewhere more at the top in the hierarchy.
	 * @param currItem The current bookmark to consider.
	 * @param considerChildren Should we traverse into the current bookmark's children? Useful to set this to
	 * {@code false} when traversing upwards in the hierarchy to prevent an infinite loop.
	 * @return Whether there is a viable bookmark following {@code currItem}.
	 */
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

	/**
	 * Recursive call that first checks if the current bookmark has any children to traverse into next, then if it
	 * has any siblings, and finally if there is a next bookmark to visit somewhere more at the top in the hierarchy.
	 * On our first iteration, get the {@code root} if it's an {@link OutlineItemCollection} itself, otherwise try to
	 * get the first child of the {@code root}.
	 * @param considerChildren Should we traverse into the current bookmark's children? Useful to set this to
	 * {@code false} when traversing upwards in the hierarchy to prevent an infinite loop.
	 * @return The next viable bookmark.
	 */
	private OutlineItemCollection next(boolean considerChildren) {
		if (currItem == null) {
			if (root instanceof OutlineItemCollection oic) {
				currItem = oic;
			} else {
				for (OutlineItemCollection oic : root) {
					currItem = oic;
					break;
				}
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
