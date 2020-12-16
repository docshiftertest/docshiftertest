package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AnnotationUtils {
	private static final Logger logger = LoggerFactory.getLogger(AnnotationUtils.class);

	private AnnotationUtils() {}

	public static ExplicitDestination extractExplicitDestination(LinkAnnotation annotation) {
		IAppointment annotationDest = annotation.getDestination();

		// If destination on the annotation itself is null,
		// we might have to deal with a GoToAction wrapping a XYZExplicitDestination here
		if (annotationDest == null) {
			PdfAction outlineAction = annotation.getAction();
			if (outlineAction instanceof GoToAction) {
				annotationDest = ((GoToAction)outlineAction).getDestination();
			}
		}

		// Check if destination is of right type
		if (!(annotationDest instanceof ExplicitDestination)) {
			if (annotationDest != null) {
				logger.debug(annotationDest + " was not a ExplicitDestination: skipping. It's a "
						+ annotationDest.getClass().getName());
			}
			return null;
		}

		ExplicitDestination dest = (ExplicitDestination)annotationDest;
		logger.debug("Appropriate link found referring to page " + dest.getPageNumber());
		return dest;
	}

	public static void setDestinationOrAction(LinkAnnotation annotation, ExplicitDestination dest) {
		if (annotation.getDestination() != null) {
			annotation.setDestination(dest);
		} else {
			annotation.setAction(new GoToAction(dest));
		}
	}

	public static XYZExplicitDestination setDestinationToUpperLeftCorner(LinkAnnotation annotation, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrAction(annotation, dest);
		return dest;
	}
}
