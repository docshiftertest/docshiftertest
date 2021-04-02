package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.Document;
import com.aspose.pdf.ExplicitDestination;
import com.aspose.pdf.GoToAction;
import com.aspose.pdf.IAppointment;
import com.aspose.pdf.IDocument;
import com.aspose.pdf.LinkAnnotation;
import com.aspose.pdf.NamedDestination;
import com.aspose.pdf.Page;
import com.aspose.pdf.XYZExplicitDestination;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class AnnotationUtils {
	private AnnotationUtils() {}

	/**
	 * Unwraps an {@link IAppointment} from a {@link LinkAnnotation} entirely and tries to cast the result as an
	 * {@link ExplicitDestination}. Will return null if it is not an {@link ExplicitDestination}.
	 * @param doc The document containing the {@link LinkAnnotation} and {@link NamedDestination}s.
	 * @param annotation The object to extract the destination from.
	 * @return The retrieved {@link ExplicitDestination} or null if it couldn't be casted or retrieved.
	 */
	public static ExplicitDestination extractExplicitDestinationSoft(IDocument doc, LinkAnnotation annotation) {
		IAppointment annotationDest = annotation.getDestination();

		// If destination on the annotation itself is null,
		// we might have to deal with a GoToAction wrapping a ExplicitDestination here
		if (annotationDest == null) {
			annotationDest = annotation.getAction();
		}

		ExplicitDestination dest = AppointmentUtils.asExplicitDestinationSoft(doc, annotationDest);
		if (dest != null) {
			log.debug("Appropriate link found referring to page {}", dest.getPageNumber());
		}
		return dest;
	}

	/**
	 * Unwraps a {@link LinkAnnotation} from an {@link IAppointment} (specifically {@link GoToAction}s) entirely and
	 * tries to cast the result as an {@link ExplicitDestination}. Will return null if it is not an
	 * {@link ExplicitDestination}. ONLY use this if you don't care about following {@link NamedDestination}s! For
	 * more robust checking see {@link #extractExplicitDestinationSoft(IDocument, LinkAnnotation)}.
	 * @param annotation The object to extract the destination from.
	 * @return The retrieved {@link ExplicitDestination} or null if it couldn't be casted or retrieved.
	 */
	public static ExplicitDestination extractExplicitDestinationHard(LinkAnnotation annotation) {
		IAppointment annotationDest = annotation.getDestination();

		// If destination on the annotation itself is null,
		// we might have to deal with a GoToAction wrapping a ExplicitDestination here
		if (annotationDest == null) {
			annotationDest = annotation.getAction();
		}

		ExplicitDestination dest = AppointmentUtils.asExplicitDestinationHard(annotationDest);
		if (dest != null) {
			log.debug("Appropriate link found referring to page {}", dest.getPageNumber());
		}
		return dest;
	}

	@Deprecated
	public static ExplicitDestination extractExplicitDestination(LinkAnnotation annotation) {
		return extractExplicitDestinationHard(annotation);
	}

	@Deprecated
	public static void setDestinationOrAction(LinkAnnotation annotation, ExplicitDestination dest) {
		setDestinationOrActionHard(annotation, dest);
	}

	public static void setDestinationOrActionHard(LinkAnnotation annotation, ExplicitDestination dest) {
		if (annotation.getDestination() != null || annotation.getAction() == null) {
			annotation.setDestination(dest);
		} else {
			annotation.setAction(new GoToAction(dest));
		}
	}

	public static void setDestinationOrActionHard(LinkAnnotation annotation, Document doc, NamedDestination dest) {
		if (annotation.getDestination() != null || annotation.getAction() == null) {
			annotation.setDestination(dest);
		} else {
			annotation.setAction(new GoToAction(doc, dest.getName()));
		}
	}

	public static void setDestinationOrActionSoft(Document doc, LinkAnnotation annotation,
												  ExplicitDestination dest) {
		if (annotation.getDestination() != null) {
			AppointmentUtils.replaceIfWrapped(doc, annotation.getDestination(), dest);
		} else if (annotation.getAction() != null) {
			if (annotation.getAction() instanceof GoToAction) {
				AppointmentUtils.replaceIfWrapped(doc, annotation.getAction(), dest);
			} else {
				annotation.setAction(new GoToAction(dest));
			}
		} else {
			annotation.setDestination(dest);
		}
	}

	public static void setDestinationOrActionSoft(Document doc, LinkAnnotation annotation,
												  NamedDestination dest) {
		if (annotation.getDestination() != null) {
			AppointmentUtils.replaceIfWrapped(doc, annotation.getDestination(), dest);
		} else if (annotation.getAction() != null) {
			if (annotation.getAction() instanceof GoToAction) {
				AppointmentUtils.replaceIfWrapped(doc, annotation.getAction(), dest);
			} else {
				annotation.setAction(new GoToAction(doc, dest.getName()));
			}
		} else {
			annotation.setDestination(dest);
		}
	}

	public static XYZExplicitDestination setDestinationToUpperLeftCornerSoft(Document doc,
																			 LinkAnnotation annotation, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrActionSoft(doc, annotation, dest);
		return dest;
	}

	public static XYZExplicitDestination setDestinationToUpperLeftCornerHard(LinkAnnotation annotation, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrActionHard(annotation, dest);
		return dest;
	}

	@Deprecated
	public static XYZExplicitDestination setDestinationToUpperLeftCorner(LinkAnnotation annotation, Page target) {
		return setDestinationToUpperLeftCornerHard(annotation, target);
	}
}
