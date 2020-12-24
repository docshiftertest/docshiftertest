package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OutlineUtils {
	private static final Logger logger = LoggerFactory.getLogger(OutlineUtils.class);

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
				logger.debug(outlineDest + " was not a ExplicitDestination: skipping. It's a "
						+ outlineDest.getClass().getName());
			}
			return null;
		}

		ExplicitDestination bookmark = (ExplicitDestination)outlineDest;
		logger.debug("Appropriate level " + outline.getLevel() + " bookmark found referring to page "
				+ bookmark.getPageNumber());
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
