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
import com.aspose.pdf.XYZExplicitDestination;

import java.util.Objects;
import java.util.stream.Stream;

public class ExplicitDestinationTransformer {
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

	public ExplicitDestinationTransformer(CustomExplicitDestination dest) {
		page = dest.getPage();
		type = null;
	}

	public ExplicitDestinationTransformer(FitBExplicitDestination dest) {
		page = dest.getPage();
		type = ExplicitDestinationType.FitB;
	}

	public ExplicitDestinationTransformer(FitBHExplicitDestination dest) {
		page = dest.getPage();
		top = dest.getTop();
		type = ExplicitDestinationType.FitBH;
	}

	public ExplicitDestinationTransformer(FitBVExplicitDestination dest) {
		page = dest.getPage();
		left = dest.getLeft();
		type = ExplicitDestinationType.FitBV;
	}

	public ExplicitDestinationTransformer(FitExplicitDestination dest) {
		page = dest.getPage();
		type = ExplicitDestinationType.Fit;
	}

	public ExplicitDestinationTransformer(FitHExplicitDestination dest) {
		page = dest.getPage();
		top = dest.getTop();
		type = ExplicitDestinationType.FitH;
	}

	public ExplicitDestinationTransformer(FitRExplicitDestination dest) {
		page = dest.getPage();
		left = dest.getLeft();
		top = dest.getTop();
		bottom = dest.getBottom();
		right = dest.getRight();
		type = ExplicitDestinationType.FitR;
	}

	public ExplicitDestinationTransformer(FitVExplicitDestination dest) {
		page = dest.getPage();
		left = dest.getLeft();
		type = ExplicitDestinationType.FitV;
	}

	public ExplicitDestinationTransformer(XYZExplicitDestination dest) {
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
		return new FitBExplicitDestination(page);
	}

	/**
	 * Vertical coordinate (top) positioned at the top edge of the window and the contents of the page magnified just
	 * enough to fit the entire width of its bounding box within the window.
	 */
	public FitBHExplicitDestination toFitBH() {
		return new FitBHExplicitDestination(page, top);
	}

	/**
	 * Horizontal coordinate (left) positioned at the left edge of the window and the contents of the page magnified
	 * just enough to fit the entire height of its bounding box within the window.
	 */
	public FitBVExplicitDestination toFitBV() {
		return new FitBVExplicitDestination(page, left);
	}

	/**
	 * Contents magnified just enough to fit the entire page within the window both horizontally and vertically. If
	 * the required horizontal and vertical magnification factors are different, use the smaller of the two, centering
	 * the page within the window in the other dimension.
	 */
	public FitExplicitDestination toFit() {
		return new FitExplicitDestination(page);
	}

	/**
	 * Vertical coordinate (top) positioned at the top edge of the window and the contents of the page magnified just
	 * enough to fit the entire width of the page within the window.
	 */
	public FitHExplicitDestination toFitH() {
		return new FitHExplicitDestination(page, top);
	}

	/**
	 * Contents magnified just enough to fit the rectangle specified by the coordinates left, bottom, right, and
	 * top entirely within the window both horizontally and vertically. If the required horizontal and vertical
	 * magnification factors are different, use the smaller of the two, centering the rectangle within the window in the
	 * other dimension.
	 */
	public FitRExplicitDestination toFitR() {
		return new FitRExplicitDestination(page, left, bottom, right, top);
	}

	/**
	 * Horizontal coordinate (left) positioned at the left edge of the window and the contents of the page magnified
	 * just enough to fit the entire height of the page within the window.
	 */
	public FitVExplicitDestination toFitV() {
		return new FitVExplicitDestination(page, left);
	}

	/**
	 * Coordinates (left, top) positioned at the upper-left corner of the window and the contents of the page
	 * magnified by the factor zoom.
	 */
	public XYZExplicitDestination toXYZ() {
		return new XYZExplicitDestination(page, left, top, zoom);
	}

	public ExplicitDestination toFitPageWithPosition() {
		if (left != null && top != null) {
			return new FitRExplicitDestination(page, left, 0, page.getRect().getURX(), top);
		}

		if (left != null) {
			return new FitVExplicitDestination(page, left);
		}

		if (top != null) {
			return new FitHExplicitDestination(page, top);
		}

		return new FitExplicitDestination(page);
	}

	public XYZExplicitDestination toCustomZoom(double zoom) {
		double left = this.left == null ? 0 : this.left;
		double top = this.top == null ? page.getRect().getURY() : this.top;
		return new XYZExplicitDestination(page, left, top, zoom);
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
