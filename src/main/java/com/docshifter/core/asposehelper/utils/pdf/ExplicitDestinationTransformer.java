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
import lombok.Getter;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

/**
 * Utility class, mainly used to transform from one {@link ExplicitDestination} to another.
 */
@Getter
public class ExplicitDestinationTransformer {
	private final ExplicitDestination origDest;
	private final Page page;
	private Supplier<Rectangle> bbox;
	private final Double left;
	private final boolean leftSet;
	private final Double top;
	private final boolean topSet;
	private final Double bottom;
	private final boolean bottomSet;
	private final Double right;
	private final boolean rightSet;
	private final Double zoom;
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
		this(null, page, null, left, top, bottom, right, zoom);
		if (left != null && right != null && left > right) {
			throw new IllegalArgumentException("Left coordinate cannot be greater than the right one.");
		}
		if (bottom != null && top != null && bottom > top) {
			throw new IllegalArgumentException("Bottom coordinate cannot be greater than the top one.");
		}
		if (zoom != null && zoom < 0) {
			throw new IllegalArgumentException("Zoom factor cannot be negative.");
		}
	}

	private ExplicitDestinationTransformer(ExplicitDestination origDest, Integer type, Double left, Double top,
										   Double bottom, Double right, Double zoom) {
		this(origDest, origDest.getPage(), type, left, top, bottom, right, zoom);
	}

	private ExplicitDestinationTransformer(ExplicitDestination origDest, Page page, Integer type, Double left, Double top,
										   Double bottom, Double right, Double zoom) {
		this.origDest = origDest;
		this.type = type;
		this.page = page;
		// Potentially expensive operation, so defer initialization until/if it's really needed and cache the result
		this.bbox = () -> {
			Rectangle bbox = page == null ? null : page.calculateContentBBox();
			this.bbox = () -> bbox;
			return bbox;
		};
		leftSet = left != null;
		this.left = leftSet ? left : calculateEdge(Rectangle::getLLX);
		topSet = top != null;
		this.top = topSet ? top : calculateEdge(Rectangle::getURY);
		bottomSet = bottom != null;
		this.bottom = bottomSet ? bottom : calculateEdge(Rectangle::getLLY);
		rightSet = right != null;
		this.right = rightSet ? right : calculateEdge(Rectangle::getURX);
		this.zoom = zoom != null ? zoom : (page == null ? null : 0d);
	}

	private Double calculateEdge(ToDoubleFunction<Rectangle> fn) {
		if (page == null) {
			return null;
		}

		Rectangle rect;
		if (type != null && (type == ExplicitDestinationType.FitB || type == ExplicitDestinationType.FitBV || type == ExplicitDestinationType.FitBH)) {
			rect = bbox.get();
		} else {
			rect = page.getRect();
		}
		return fn.applyAsDouble(rect);
	}

	public ExplicitDestinationTransformer(CustomExplicitDestination dest) {
		this(dest, null, null, null, null, null, null);
	}

	public ExplicitDestinationTransformer(FitBExplicitDestination dest) {
		this(dest, ExplicitDestinationType.FitB, null, null, null, null, null);
	}

	public ExplicitDestinationTransformer(FitBHExplicitDestination dest) {
		this(dest, ExplicitDestinationType.FitBH, null, dest.getTop(), null, null, null);
	}

	public ExplicitDestinationTransformer(FitBVExplicitDestination dest) {
		this(dest, ExplicitDestinationType.FitBV, dest.getLeft(), null, null, null, null);
	}

	public ExplicitDestinationTransformer(FitExplicitDestination dest) {
		this(dest, ExplicitDestinationType.Fit, null, null, null, null, null);
	}

	public ExplicitDestinationTransformer(FitHExplicitDestination dest) {
		this(dest, ExplicitDestinationType.FitH, null, dest.getTop(), null, null, null);
	}

	public ExplicitDestinationTransformer(FitRExplicitDestination dest) {
		this(dest, ExplicitDestinationType.FitR, dest.getLeft(), dest.getTop(), dest.getBottom(), dest.getRight(), null);
	}

	public ExplicitDestinationTransformer(FitVExplicitDestination dest) {
		this(dest, ExplicitDestinationType.FitV, dest.getLeft(), null, null, null, null);
	}

	public ExplicitDestinationTransformer(XYZExplicitDestination dest) {
		this(dest, ExplicitDestinationType.XYZ, dest.getLeft(), dest.getTop(), null, null, dest.getZoom());
	}

	/**
	 * Contents magnified just enough to fit its bounding box entirely within the window both horizontally and
	 * vertically. If the required horizontal and vertical magnification factors are different, use the smaller of the
	 * two, centering the bounding box within the window in the other dimension.
	 */
	public FitBExplicitDestination toFitB() {
		if (Objects.equals(type, ExplicitDestinationType.FitB)) {
			return (FitBExplicitDestination) origDest;
		}
		return new FitBExplicitDestination(page);
	}

	/**
	 * Vertical coordinate (top) positioned at the top edge of the window and the contents of the page magnified just
	 * enough to fit the entire width of its bounding box within the window.
	 */
	public FitBHExplicitDestination toFitBH() {
		if (Objects.equals(type, ExplicitDestinationType.FitBH)) {
			return (FitBHExplicitDestination) origDest;
		}
		return new FitBHExplicitDestination(page, top);
	}

	/**
	 * Horizontal coordinate (left) positioned at the left edge of the window and the contents of the page magnified
	 * just enough to fit the entire height of its bounding box within the window.
	 */
	public FitBVExplicitDestination toFitBV() {
		if (Objects.equals(type, ExplicitDestinationType.FitBV)) {
			return (FitBVExplicitDestination) origDest;
		}
		return new FitBVExplicitDestination(page, left);
	}

	/**
	 * Contents magnified just enough to fit the entire page within the window both horizontally and vertically. If
	 * the required horizontal and vertical magnification factors are different, use the smaller of the two, centering
	 * the page within the window in the other dimension.
	 */
	public FitExplicitDestination toFit() {
		if (Objects.equals(type, ExplicitDestinationType.Fit)) {
			return (FitExplicitDestination) origDest;
		}
		return new FitExplicitDestination(page);
	}

	/**
	 * Vertical coordinate (top) positioned at the top edge of the window and the contents of the page magnified just
	 * enough to fit the entire width of the page within the window.
	 */
	public FitHExplicitDestination toFitH() {
		if (Objects.equals(type, ExplicitDestinationType.FitH)) {
			return (FitHExplicitDestination) origDest;
		}
		return new FitHExplicitDestination(page, top);
	}

	/**
	 * Contents magnified just enough to fit the rectangle specified by the coordinates left, bottom, right, and
	 * top entirely within the window both horizontally and vertically. If the required horizontal and vertical
	 * magnification factors are different, use the smaller of the two, centering the rectangle within the window in the
	 * other dimension.
	 */
	public FitRExplicitDestination toFitR() {
		if (Objects.equals(type, ExplicitDestinationType.FitR)) {
			return (FitRExplicitDestination) origDest;
		}
		// NOTE: Stoopid Asspose can't even seem to document their stoopid code right!!!
		// The 3rd argument is actually top, not bottom!
		return new FitRExplicitDestination(page, left, top, right, bottom);
	}

	/**
	 * Horizontal coordinate (left) positioned at the left edge of the window and the contents of the page magnified
	 * just enough to fit the entire height of the page within the window.
	 */
	public FitVExplicitDestination toFitV() {
		if (Objects.equals(type, ExplicitDestinationType.FitV)) {
			return (FitVExplicitDestination) origDest;
		}
		return new FitVExplicitDestination(page, left);
	}

	/**
	 * Coordinates (left, top) positioned at the upper-left corner of the window and the contents of the page
	 * magnified by the factor zoom.
	 */
	public XYZExplicitDestination toXYZ() {
		if (Objects.equals(type, ExplicitDestinationType.XYZ)) {
			return (XYZExplicitDestination) origDest;
		}
		return new XYZExplicitDestination(page, left, top, zoom);
	}

	/**
	 * Converts to an appropriate Fit (page edges) {@link ExplicitDestination}.
	 */
	public ExplicitDestination toFitWithPosition() {
		if (bottomSet || rightSet || (leftSet && topSet)) {
			return toFitR();
		}

		if (leftSet) {
			return toFitV();
		}

		if (topSet) {
			return toFitH();
		}

		return toFit();
	}

	/**
	 * Converts to an appropriate Fit BBox/Visible {@link ExplicitDestination}.
	 * @return
	 */
	public ExplicitDestination toFitVisibleWithPosition() {
		if (bottomSet || rightSet || (leftSet && topSet)) {
			double visibleLeft = clampXBetweenBBox(left);
			double visibleRight = clampXBetweenBBox(right);
			double visibleBottom = clampYBetweenBBox(bottom);
			double visibleTop = clampYBetweenBBox(top);
			return new FitRExplicitDestination(page, visibleLeft, visibleBottom, visibleRight, visibleTop);
		}

		if (leftSet) {
			return toFitBV();
		}

		if (topSet) {
			return toFitBH();
		}

		return toFitB();
	}

	private double clampXBetweenBBox(double value) {
		return Math.max(bbox.get().getLLX(), Math.min(bbox.get().getURX(), value));
	}

	private double clampYBetweenBBox(double value) {
		return Math.max(bbox.get().getLLY(), Math.min(bbox.get().getURY(), value));
	}

	public XYZExplicitDestination toCustomZoom(double zoom) {
		if (Objects.equals(this.zoom, zoom)) {
			return toXYZ();
		}
		return new XYZExplicitDestination(page, left, top, zoom);
	}

	public Point getTopLeft() {
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
		if (origDest != null && page != null && page.getNumber() == newPageNum) {
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

	/**
	 * Changes the page of the {@link ExplicitDestination} that was passed to this transformer.
	 * @param page The new page
	 * @return An {@link ExplicitDestination} of the same type and with the same values, but that has a different page.
	 * @throws IllegalStateException The {@link ExplicitDestination} is of type {@code Custom}, which does not support
	 * page numbers.
	 */
	public ExplicitDestination changePage(Page page) {
		if (origDest != null && this.page != null && this.page.equals(page)) {
			return origDest;
		}
		if (type == null) {
			throw new IllegalStateException("Cannot change page of a Custom explicit destination type.");
		}
		double[] values = Stream.of(left, top, bottom, right, zoom)
				.filter(Objects::nonNull)
				.mapToDouble(d -> d)
				.toArray();
		return ExplicitDestination.createDestination(page, type, values);
	}
}
