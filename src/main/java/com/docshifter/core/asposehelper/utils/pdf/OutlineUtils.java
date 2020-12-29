package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.*;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class OutlineUtils {
	private OutlineUtils() {}

	public static ExplicitDestination extractExplicitDestination(OutlineItemCollection outline) {
		IAppointment outlineDest = outline.getDestination();

		// If destination on the outline itself is null,
		// we might have to deal with a GoToAction wrapping a XYZExplicitDestination here
		if (outlineDest == null) {
			PdfAction outlineAction = outline.getAction();
			if (outlineAction instanceof GoToAction) {
				outlineDest = ((GoToAction)outlineAction).getDestination();
			}
		}

		// Check if destination is of right type
		if (!(outlineDest instanceof ExplicitDestination)) {
			if (outlineDest != null) {
				log.debug("{} was not a ExplicitDestination: skipping. It's a {}", outlineDest,
						outlineDest.getClass().getName());
			}
			return null;
		}

		ExplicitDestination bookmark = (ExplicitDestination)outlineDest;
		log.debug("Appropriate level {} bookmark found referring to page {}", outline.getLevel(),
				bookmark.getPageNumber());
		return bookmark;
	}

	public static void moveOutline(OutlineItemCollection oldRoot, OutlineItemCollection newRoot) {
		Outlines oldParent = oldRoot.getParent();
		newRoot.add(oldRoot);
		oldParent.remove(oldRoot);
	}

	public static void setDestinationOrAction(OutlineItemCollection outline, ExplicitDestination dest) {
		if (outline.getDestination() != null) {
			outline.setDestination(dest);
		} else {
			outline.setAction(new GoToAction(dest));
		}
	}

	public static XYZExplicitDestination setDestinationToUpperLeftCorner(OutlineItemCollection outline, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrAction(outline, dest);
		return dest;
	}
}
