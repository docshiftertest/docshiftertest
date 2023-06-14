package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.CustomExplicitDestination;
import com.aspose.pdf.ExplicitDestination;
import com.aspose.pdf.ExplicitDestinationType;
import com.aspose.pdf.FitBExplicitDestination;
import com.aspose.pdf.FitBHExplicitDestination;
import com.aspose.pdf.FitBVExplicitDestination;
import com.aspose.pdf.FitExplicitDestination;
import com.aspose.pdf.FitHExplicitDestination;
import com.aspose.pdf.FitRExplicitDestination;
import com.aspose.pdf.FitVExplicitDestination;
import com.aspose.pdf.Page;
import com.aspose.pdf.Point;
import com.aspose.pdf.Rectangle;
import com.aspose.pdf.XYZExplicitDestination;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utility class, mainly used to transform from one {@link ExplicitDestination} to another.
 */
public class ExplicitDestinationTransformer {
	private final ExplicitDestination origDest;
	private final Page page;
	private Double left;
	private Double top;
	private Double bottom;
	private Double right;
	private Double zoom;
	private final Integer type;

	public static ExplicitDestinationTransformer create(ExplicitDestination dest) {
		if (dest == null) {
			throw new NullPointerException("Destination cannot be null.");
		}

		if (dest instanceof CustomExplicitDestination) {
			return new ExplicitDestinationTransformer((CustomExplicitDestination) dest);
		} else if (dest instanceof FitBExplicitDestination) {
			return new ExplicitDestinationTransformer((FitBExplicitDestination) dest);
		} else if (dest instanceof FitBHExplicitDestination) {
			return new ExplicitDestinationTransformer((FitBHExplicitDestination) dest);
		} else if (dest instanceof FitBVExplicitDestination) {
			return new ExplicitDestinationTransformer((FitBVExplicitDestination) dest);
		} else if (dest instanceof FitExplicitDestination) {
			return new ExplicitDestinationTransformer((FitExplicitDestination) dest);
		} else if (dest instanceof FitRExplicitDestination) {
			return new ExplicitDestinationTransformer((FitRExplicitDestination) dest);
		} else if (dest instanceof FitHExplicitDestination) {
			return new ExplicitDestinationTransformer((FitHExplicitDestination) dest);
		} else if (dest instanceof FitVExplicitDestination) {
			return new ExplicitDestinationTransformer((FitVExplicitDestination) dest);
		} else if (dest instanceof XYZExplicitDestination) {
			return new ExplicitDestinationTransformer((XYZExplicitDestination) dest);
		} else {
			throw new IllegalArgumentException("Explicit destination subtype " + dest.getClass().getName() + " is not " +
					"supported.");
		}
	}

	/**
	 * Creates a new {@link ExplicitDestinationTransformer} that is not immediately derived from a previously existing
	 * {@link ExplicitDestination}.
	 * @param page The {@link Page} that should be linked to the {@link ExplicitDestination}.
	 * @param left The (optional) left coordinate.
	 * @param top The (optional) top coordinate.
	 * @param bottom The (optional) bottom coordinate.
	 * @param right The (optional) right coordinate.
	 * @param zoom The (optional) zoom.
	 */
	public ExplicitDestinationTransformer(Page page, Double left, Double top, Double bottom, Double right, Double zoom) {
		if (left != null && right != null && left > right) {
			throw new IllegalArgumentException("Left coordinate cannot be greater than the right one.");
		}
		if (bottom != null && top != null && bottom > top) {
			throw new IllegalArgumentException("Bottom coordinate cannot be greater than the top one.");
		}
		if (zoom != null && zoom < 0) {
			throw new IllegalArgumentException("Zoom factor cannot be negative.");
		}
		origDest = null;
		type = null;
		this.page = page;
		this.left = left;
		this.top = top;
		this.bottom = bottom;
		this.right = right;
		this.zoom = zoom;
	}

	public ExplicitDestinationTransformer(CustomExplicitDestination dest) {
		origDest = dest;
		page = dest.getPage();
		type = null;
	}

	public ExplicitDestinationTransformer(FitBExplicitDestination dest) {
		origDest = dest;
		page = dest.getPage();
		type = ExplicitDestinationType.FitB;
	}

	public ExplicitDestinationTransformer(FitBHExplicitDestination dest) {
		origDest = dest;
		page = dest.getPage();
		top = dest.getTop();
		type = ExplicitDestinationType.FitBH;
	}

	public ExplicitDestinationTransformer(FitBVExplicitDestination dest) {
		origDest = dest;
		page = dest.getPage();
		left = dest.getLeft();
		type = ExplicitDestinationType.FitBV;
	}

	public ExplicitDestinationTransformer(FitExplicitDestination dest) {
		origDest = dest;
		page = dest.getPage();
		type = ExplicitDestinationType.Fit;
	}

	public ExplicitDestinationTransformer(FitHExplicitDestination dest) {
		origDest = dest;
		page = dest.getPage();
		top = dest.getTop();
		type = ExplicitDestinationType.FitH;
	}

	public ExplicitDestinationTransformer(FitRExplicitDestination dest) {
		origDest = dest;
		page = dest.getPage();
		left = dest.getLeft();
		top = dest.getTop();
		bottom = dest.getBottom();
		right = dest.getRight();
		type = ExplicitDestinationType.FitR;
	}

	public ExplicitDestinationTransformer(FitVExplicitDestination dest) {
		origDest = dest;
		page = dest.getPage();
		left = dest.getLeft();
		type = ExplicitDestinationType.FitV;
	}

	public ExplicitDestinationTransformer(XYZExplicitDestination dest) {
		origDest = dest;
		page = dest.getPage();
		left = dest.getLeft();
		top = dest.getTop();
		zoom = dest.getZoom();
		type = ExplicitDestinationType.XYZ;
	}

	/**
	 * Contents magnified just enough to fit its bounding box entirely within the window both horizontally and
	 * vertically. If the required horizontal and vertical magnification factors are different, use the smaller of the
	 * two, centering the bounding box within the window in the other dimension.
	 */
	public FitBExplicitDestination toFitB() {
		if (type == ExplicitDestinationType.FitB) {
			return (FitBExplicitDestination) origDest;
		}
		return new FitBExplicitDestination(page);
	}

	/**
	 * Vertical coordinate (top) positioned at the top edge of the window and the contents of the page magnified just
	 * enough to fit the entire width of its bounding box within the window.
	 */
	public FitBHExplicitDestination toFitBH() {
		if (type == ExplicitDestinationType.FitBH) {
			return (FitBHExplicitDestination) origDest;
		}
		return new FitBHExplicitDestination(page, Objects.requireNonNullElse(top, page.getRect().getHeight()));
	}

	/**
	 * Horizontal coordinate (left) positioned at the left edge of the window and the contents of the page magnified
	 * just enough to fit the entire height of its bounding box within the window.
	 */
	public FitBVExplicitDestination toFitBV() {
		if (type == ExplicitDestinationType.FitBV) {
			return (FitBVExplicitDestination) origDest;
		}
		return new FitBVExplicitDestination(page, Objects.requireNonNullElse(left, 0d));
	}

	/**
	 * Contents magnified just enough to fit the entire page within the window both horizontally and vertically. If
	 * the required horizontal and vertical magnification factors are different, use the smaller of the two, centering
	 * the page within the window in the other dimension.
	 */
	public FitExplicitDestination toFit() {
		if (type == ExplicitDestinationType.Fit) {
			return (FitExplicitDestination) origDest;
		}
		return new FitExplicitDestination(page);
	}

	/**
	 * Vertical coordinate (top) positioned at the top edge of the window and the contents of the page magnified just
	 * enough to fit the entire width of the page within the window.
	 */
	public FitHExplicitDestination toFitH() {
		if (type == ExplicitDestinationType.FitH) {
			return (FitHExplicitDestination) origDest;
		}
		return new FitHExplicitDestination(page, Objects.requireNonNullElse(top, page.getRect().getHeight()));
	}

	/**
	 * Contents magnified just enough to fit the rectangle specified by the coordinates left, bottom, right, and
	 * top entirely within the window both horizontally and vertically. If the required horizontal and vertical
	 * magnification factors are different, use the smaller of the two, centering the rectangle within the window in the
	 * other dimension.
	 */
	public FitRExplicitDestination toFitR() {
		if (type == ExplicitDestinationType.FitR) {
			return (FitRExplicitDestination) origDest;
		}
		return new FitRExplicitDestination(page, Objects.requireNonNullElse(left, 0d),
				Objects.requireNonNullElse(bottom, 0d), Objects.requireNonNullElse(right, page.getRect().getWidth()),
				Objects.requireNonNullElse(top, page.getRect().getHeight()));
	}

	/**
	 * Horizontal coordinate (left) positioned at the left edge of the window and the contents of the page magnified
	 * just enough to fit the entire height of the page within the window.
	 */
	public FitVExplicitDestination toFitV() {
		if (type == ExplicitDestinationType.FitV) {
			return (FitVExplicitDestination) origDest;
		}
		return new FitVExplicitDestination(page, Objects.requireNonNullElse(left, 0d));
	}

	/**
	 * Coordinates (left, top) positioned at the upper-left corner of the window and the contents of the page
	 * magnified by the factor zoom.
	 */
	public XYZExplicitDestination toXYZ() {
		if (type == ExplicitDestinationType.XYZ) {
			return (XYZExplicitDestination) origDest;
		}
		return new XYZExplicitDestination(page, Objects.requireNonNullElse(left, 0d),
				Objects.requireNonNullElse(top, page.getRect().getHeight()), Objects.requireNonNullElse(zoom, 0d));
	}

	/**
	 * Converts to an appropriate Fit (page edges) {@link ExplicitDestination}.
	 */
	public ExplicitDestination toFitWithPosition() {
		if (bottom != null || right != null || (left != null && top != null)) {
			return toFitR();
		}

		if (left != null) {
			return toFitV();
		}

		if (top != null) {
			return toFitH();
		}

		return toFit();
	}

	/**
	 * Converts to an appropriate Fit BBox/Visible {@link ExplicitDestination}.
	 * @return
	 */
	public ExplicitDestination toFitVisibleWithPosition() {
		if (bottom != null || right != null || (left != null && top != null)) {
			Rectangle bbox = page.calculateContentBBox();
			double visibleLeft = clampXBetweenBBox(bbox, left, 0d);
			double visibleRight = clampXBetweenBBox(bbox, right, page.getRect().getWidth());
			double visibleBottom = clampYBetweenBBox(bbox, bottom, 0d);
			double visibleTop = clampYBetweenBBox(bbox, top, page.getRect().getHeight());
			return new FitRExplicitDestination(page, visibleLeft, visibleBottom, visibleRight, visibleTop);
		}

		if (left != null) {
			return toFitBV();
		}

		if (top != null) {
			return toFitBH();
		}

		return toFitB();
	}

	private double clampXBetweenBBox(Rectangle bbox, Double value, double defaultValueIfNull) {
		return Math.max(bbox.getLLX(), Math.min(bbox.getURX(), Objects.requireNonNullElse(value, defaultValueIfNull)));
	}

	private double clampYBetweenBBox(Rectangle bbox, Double value, double defaultValueIfNull) {
		return Math.max(bbox.getLLY(), Math.min(bbox.getURY(), Objects.requireNonNullElse(value, defaultValueIfNull)));
	}

	public XYZExplicitDestination toCustomZoom(double zoom) {
		if (this.zoom != null && this.zoom == zoom) {
			return toXYZ();
		}
		Point topLeft = getTopLeft();
		return new XYZExplicitDestination(page, topLeft.getX(), topLeft.getY(), zoom);
	}

	public Point getTopLeft() {
		double left = this.left == null ? 0 : this.left;
		double top = this.top == null ? page.getRect().getHeight() : this.top;
		return new Point(left, top);
	}

	public XYZExplicitDestination toActualSize() {
		return toCustomZoom(1);
	}

	public XYZExplicitDestination toInheritZoom() {
		return toCustomZoom(0);
	}

	/**
	 * Changes the page number of the {@link ExplicitDestination} that was passed to this transformer.
	 * @param newPageNum The new page number.
	 * @return An {@link ExplicitDestination} of the same type and with the same values, but that has a different page.
	 * @throws IllegalStateException The {@link ExplicitDestination} is of type {@code Custom}, which does not support
	 * page numbers.
	 */
	public ExplicitDestination changePage(int newPageNum) {
		if (page.getNumber() == newPageNum) {
			return origDest;
		}
		if (type == null) {
			throw new IllegalStateException("Cannot change page of a Custom explicit destination type.");
		}
		double[] values = Stream.of(left, top, bottom, right, zoom)
				.filter(Objects::nonNull)
				.mapToDouble(d -> d)
				.toArray();
		return ExplicitDestination.createDestination(newPageNum, type, values);
	}
}
