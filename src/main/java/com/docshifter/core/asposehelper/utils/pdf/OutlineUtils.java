package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.Document;
import com.aspose.pdf.ExplicitDestination;
import com.aspose.pdf.GoToAction;
import com.aspose.pdf.IAppointment;
import com.aspose.pdf.IDocument;
import com.aspose.pdf.NamedDestination;
import com.aspose.pdf.OutlineItemCollection;
import com.aspose.pdf.Outlines;
import com.aspose.pdf.Page;
import com.aspose.pdf.XYZExplicitDestination;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class OutlineUtils {
	private OutlineUtils() {}

	/**
	 * Get
	 * @param doc
	 * @param outline
	 * @return
	 */
	public static ExplicitDestination extractExplicitDestinationSoft(IDocument doc, OutlineItemCollection outline) {
		IAppointment outlineDest = outline.getDestination();

		// If destination on the outline itself is null,
		// we might have to deal with a GoToAction wrapping a ExplicitDestination here
		if (outlineDest == null) {
			outlineDest = outline.getAction();
		}

		ExplicitDestination bookmark = AppointmentUtils.asExplicitDestinationSoft(doc, outlineDest);
		if (bookmark != null) {
			log.debug("Appropriate level {} bookmark found referring to page {}", outline.getLevel(),
					bookmark.getPageNumber());
		}
		return bookmark;
	}

	public static ExplicitDestination extractExplicitDestinationHard(OutlineItemCollection outline) {
		IAppointment outlineDest = outline.getDestination();

		// If destination on the outline itself is null,
		// we might have to deal with a GoToAction wrapping a ExplicitDestination here
		if (outlineDest == null) {
			outlineDest = outline.getAction();
		}

		ExplicitDestination bookmark = AppointmentUtils.asExplicitDestinationHard(outlineDest);
		if (bookmark != null) {
			log.debug("Appropriate level {} bookmark found referring to page {}", outline.getLevel(),
					bookmark.getPageNumber());
		}
		return bookmark;
	}

	@Deprecated
	public static ExplicitDestination extractExplicitDestination(OutlineItemCollection outline) {
		return extractExplicitDestinationHard(outline);
	}

	public static void moveOutline(OutlineItemCollection oldRoot, OutlineItemCollection newRoot) {
		Outlines oldParent = oldRoot.getParent();
		newRoot.add(oldRoot);
		oldParent.remove(oldRoot);
	}

	@Deprecated
	public static void setDestinationOrAction(OutlineItemCollection outline, ExplicitDestination dest) {
		setDestinationOrActionHard(outline, dest);
	}

	public static void setDestinationOrActionHard(OutlineItemCollection outline, ExplicitDestination dest) {
		if (outline.getDestination() != null || outline.getAction() == null) {
			outline.setDestination(dest);
		} else {
			outline.setAction(new GoToAction(dest));
		}
	}

	public static void setDestinationOrActionHard(OutlineItemCollection outline, Document doc, NamedDestination dest) {
		if (outline.getDestination() != null || outline.getAction() == null) {
			outline.setDestination(dest);
		} else {
			outline.setAction(new GoToAction(doc, dest.getName()));
		}
	}

	public static void setDestinationOrActionSoft(Document doc, OutlineItemCollection outline,
												  ExplicitDestination dest) {
		if (outline.getDestination() != null) {
			AppointmentUtils.replaceIfWrapped(doc, outline.getDestination(), dest);
		} else if (outline.getAction() != null) {
			if (outline.getAction() instanceof GoToAction) {
				AppointmentUtils.replaceIfWrapped(doc, outline.getAction(), dest);
			} else {
				outline.setAction(new GoToAction(dest));
			}
		} else {
			outline.setDestination(dest);
		}
	}

	public static void setDestinationOrActionSoft(Document doc, OutlineItemCollection outline,
												  NamedDestination dest) {
		if (outline.getDestination() != null) {
			AppointmentUtils.replaceIfWrapped(doc, outline.getDestination(), dest);
		} else if (outline.getAction() != null) {
			if (outline.getAction() instanceof GoToAction) {
				AppointmentUtils.replaceIfWrapped(doc, outline.getAction(), dest);
			} else {
				outline.setAction(new GoToAction(doc, dest.getName()));
			}
		} else {
			outline.setDestination(dest);
		}
	}

	public static XYZExplicitDestination setDestinationToUpperLeftCornerSoft(Document doc,
																			 OutlineItemCollection outline, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrActionSoft(doc, outline, dest);
		return dest;
	}

	public static XYZExplicitDestination setDestinationToUpperLeftCornerHard(OutlineItemCollection outline, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrActionHard(outline, dest);
		return dest;
	}

	@Deprecated
	public static XYZExplicitDestination setDestinationToUpperLeftCorner(OutlineItemCollection outline, Page target) {
		return setDestinationToUpperLeftCornerHard(outline, target);
	}
}
