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
	 * Extracts an {@link IAppointment} from a {@link LinkAnnotation} entirely and tries to cast the result as an
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
	 * Extracts an {@link IAppointment} from a {@link LinkAnnotation} (following specifically through
	 * {@link GoToAction}s) entirely and tries to cast the result as an {@link ExplicitDestination}. Will return null
	 * if it is not an {@link ExplicitDestination}. ONLY use this if you don't care about following
	 * {@link NamedDestination}s! For more robust checking see
	 * {@link #extractExplicitDestinationSoft(IDocument, LinkAnnotation)}.
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

	/**
	 * Do NOT use this, has been replaced by {@link #extractExplicitDestinationHard(LinkAnnotation)} in order to make
	 * intentions clearer, but {@link #extractExplicitDestinationSoft(IDocument, LinkAnnotation)} should be used in
	 * most scenarios for more robust checking.
	 * @param annotation The object to extract the destination from.
	 * @return The retrieved {@link ExplicitDestination} or null if it couldn't be casted or retrieved.
	 */
	@Deprecated
	public static ExplicitDestination extractExplicitDestination(LinkAnnotation annotation) {
		return extractExplicitDestinationHard(annotation);
	}

	/**
	 * Do NOT use this, has been replaced by {@link #setDestinationOrActionHard(LinkAnnotation, ExplicitDestination)}
	 * in order to make intentions clearer. Also see
	 * {@link #setDestinationOrActionSoft(Document, LinkAnnotation, ExplicitDestination)} and the overloaded methods
	 * that accept a {@link NamedDestination}.
	 * @param annotation The annotation to change.
	 * @param dest The destination to change to.
	 */
	@Deprecated
	public static void setDestinationOrAction(LinkAnnotation annotation, ExplicitDestination dest) {
		setDestinationOrActionHard(annotation, dest);
	}

	/**
	 * Changes the underlying destination of a {@link LinkAnnotation} to an {@link ExplicitDestination}. Does NOT
	 * follow through wrapped {@link IAppointment}s if there are any, so this is useful if you want to change the
	 * destination of a single link instead of the destination backed by a {@link NamedDestination} for example
	 * (which would then update the destinations of everything pointing to that specific name).
	 * @param annotation The annotation to change.
	 * @param dest The destination to change to.
	 */
	public static void setDestinationOrActionHard(LinkAnnotation annotation, ExplicitDestination dest) {
		// If there is already a destination set it. If there is no destination or action at all, set the destination
		// instead of the action as a sane default.
		if (annotation.getDestination() != null || annotation.getAction() == null) {
			annotation.setDestination(dest);
		}

		if (annotation.getAction() != null) {
			annotation.setAction(new GoToAction(dest));
		}
	}

	/**
	 * Changes the underlying destination of a {@link LinkAnnotation} to a {@link NamedDestination}. Does NOT follow
	 * through wrapped {@link IAppointment}s if there are any, so this is useful if you want to change the
	 * destination of a single link instead of the destination backed by a {@link NamedDestination} for example
	 * (which would then update the destinations of everything pointing to that specific name).
	 * @param annotation The annotation to change.
	 * @param doc The document that contains the annotation.
	 * @param dest The destination to change to.
	 */
	public static void setDestinationOrActionHard(LinkAnnotation annotation, Document doc, NamedDestination dest) {
		// If there is already a destination set it. If there is no destination or action at all, set the destination
		// instead of the action as a sane default.
		if (annotation.getDestination() != null || annotation.getAction() == null) {
			annotation.setDestination(dest);
		}

		if (annotation.getAction() != null) {
			annotation.setAction(new GoToAction(doc, dest.getName()));
		}
	}

	/**
	 * Changes the underlying destination of a {@link LinkAnnotation} to an {@link ExplicitDestination}. DOES follow
	 * through wrapped {@link IAppointment}s if there are any, so this is useful if you want to or don't care about
	 * changing the destination of everything pointing to a {@link NamedDestination}.
	 * @param annotation The annotation to change.
	 * @param doc The document that contains the annotation.
	 * @param dest The destination to change to.
	 */
	public static void setDestinationOrActionSoft(Document doc, LinkAnnotation annotation,
												  ExplicitDestination dest) {
		boolean updated = false;
		if (annotation.getDestination() != null) {
			annotation.setDestination(AppointmentUtils.replaceIfWrapped(doc, annotation.getDestination(), dest));
			updated = true;
		}

		if (annotation.getAction() != null) {
			if (annotation.getAction() instanceof GoToAction) {
				AppointmentUtils.replaceIfWrapped(doc, annotation.getAction(), dest);
			} else {
				annotation.setAction(new GoToAction(dest));
			}
			updated = true;
		}

		if (!updated) {
			annotation.setDestination(dest);
		}
	}

	/**
	 * Changes the underlying destination of a {@link LinkAnnotation} to a {@link NamedDestination}. DOES follow
	 * through wrapped {@link IAppointment}s if there are any, so this is useful if you want to or don't care about
	 * changing the destination of everything pointing to a {@link NamedDestination}.
	 * @param annotation The annotation to change.
	 * @param doc The document that contains the annotation.
	 * @param dest The destination to change to.
	 */
	public static void setDestinationOrActionSoft(Document doc, LinkAnnotation annotation,
												  NamedDestination dest) {
		boolean updated = false;
		if (annotation.getDestination() != null) {
			annotation.setDestination(AppointmentUtils.replaceIfWrapped(doc, annotation.getDestination(), dest));
			updated = true;
		}

		if (annotation.getAction() != null) {
			if (annotation.getAction() instanceof GoToAction) {
				AppointmentUtils.replaceIfWrapped(doc, annotation.getAction(), dest);
			} else {
				annotation.setAction(new GoToAction(doc, dest.getName()));
			}
			updated = true;
		}

		if (!updated) {
			annotation.setDestination(dest);
		}
	}

	/**
	 * Changes the underlying destination of a {@link LinkAnnotation} to an {@link ExplicitDestination} pointing to
	 * the top left of a page. DOES follow through wrapped {@link IAppointment}s if there are any, so this is useful
	 * if you want to or don't care about changing the destination of everything pointing to a {@link NamedDestination}.
	 * @param annotation The annotation to change.
	 * @param doc The document that contains the annotation.
	 * @param target The target page to point to.
	 */
	public static XYZExplicitDestination setDestinationToUpperLeftCornerSoft(Document doc,
																			 LinkAnnotation annotation, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrActionSoft(doc, annotation, dest);
		return dest;
	}

	/**
	 * Changes the underlying destination of a {@link LinkAnnotation} to an {@link ExplicitDestination} pointing to the
	 * top left of a page. Does NOT follow through wrapped {@link IAppointment}s if there are any, so this is useful
	 * if you want to change the destination of a single link instead of the destination backed by a
	 * {@link NamedDestination} for example (which would then update the destinations of everything pointing to that
	 * specific name).
	 * @param annotation The annotation to change.
	 * @param target The target page to point to.
	 */
	public static XYZExplicitDestination setDestinationToUpperLeftCornerHard(LinkAnnotation annotation, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrActionHard(annotation, dest);
		return dest;
	}

	/**
	 * Do NOT use this, has been replaced by {@link #setDestinationToUpperLeftCornerHard(LinkAnnotation, Page)}
	 * in order to make intentions clearer.
	 * @param annotation The annotation to change.
	 * @param target The target page to point to.
	 */
	@Deprecated
	public static XYZExplicitDestination setDestinationToUpperLeftCorner(LinkAnnotation annotation, Page target) {
		return setDestinationToUpperLeftCornerHard(annotation, target);
	}
}
