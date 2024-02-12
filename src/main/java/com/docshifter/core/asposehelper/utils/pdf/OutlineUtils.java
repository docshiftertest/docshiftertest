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
import com.aspose.pdf.PdfAction;
import com.aspose.pdf.XYZExplicitDestination;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Log4j2
public final class OutlineUtils {
	private OutlineUtils() {}

	/**
	 * Extracts an {@link IAppointment} from a {@link OutlineItemCollection} by visiting its destination or action.
	 * @param outline The object to extract the destination from.
	 * @return The retrieved {@link IAppointment}, which can be null.
	 */
	public static IAppointment extractAppointment(OutlineItemCollection outline) {
		IAppointment outlineDest = outline.getDestination();

		// If destination on the outline itself is null,
		// we might have to deal with a GoToAction wrapping a ExplicitDestination here
		if (outlineDest == null) {
			outlineDest = outline.getAction();
		}

		return outlineDest;
	}

	/**
	 * Extracts an {@link IAppointment} from an {@link OutlineItemCollection} entirely and tries to cast the result
	 * as an {@link ExplicitDestination}. Will return null if it is not an {@link ExplicitDestination}.
	 * @param doc The document containing the {@link OutlineItemCollection} and {@link NamedDestination}s.
	 * @param outline The object to extract the destination from.
	 * @return The retrieved {@link ExplicitDestination} or null if it couldn't be casted or retrieved.
	 */
	public static ExplicitDestination extractExplicitDestinationSoft(IDocument doc, OutlineItemCollection outline) {
		return extractExplicitDestination(outline, doc);
	}

	/**
	 * Extracts an {@link IAppointment} from an {@link OutlineItemCollection} (following specifically through
	 * {@link GoToAction}s) entirely and tries to cast the result as an {@link ExplicitDestination}. Will return null
	 * if it is not an {@link ExplicitDestination}. ONLY use this if you don't care about following
	 * {@link NamedDestination}s! For more robust checking see
	 * {@link #extractExplicitDestinationSoft(IDocument, OutlineItemCollection)}.
	 * @param outline The object to extract the destination from.
	 * @return The retrieved {@link ExplicitDestination} or null if it couldn't be casted or retrieved.
	 */
	public static ExplicitDestination extractExplicitDestinationHard(OutlineItemCollection outline) {
		return extractExplicitDestination(outline, null);
	}

	/**
	 * Extracts an {@link IAppointment} from an {@link OutlineItemCollection} entirely and tries to cast the result
	 * as an {@link ExplicitDestination}. Will return null if it is not an {@link ExplicitDestination}.
	 * @param doc The document containing the {@link OutlineItemCollection} and {@link NamedDestination}s.
	 * @param outline The object to extract the destination from.
	 * @return The retrieved {@link ExplicitDestination} or null if it couldn't be cast or retrieved.
	 */
	private static ExplicitDestination extractExplicitDestination(OutlineItemCollection outline, IDocument doc) {

		// First we try to get directly from the destination
		IAppointment outlineDest = outline.getDestination();

		// If we don't have a doc, it means this method was called by extractExplicitDestinationHard
		boolean isHard = doc == null;

		ExplicitDestination bookmark = null;

		if (outlineDest != null) {
			bookmark = isHard
					? AppointmentUtils.asExplicitDestinationHard(outlineDest)
					: AppointmentUtils.asExplicitDestinationSoft(doc, outlineDest);
		}

		// If the bookmark is null or the page inside the destination is null, we should try to get the action
		// We learned with DSB-2141 - DocShifter keeps crashing at Dexcom
		// That even when we have a destination, the page can be null
		// In this case we have the page in the action
		if (bookmark == null || bookmark.getPage() == null) {

			outlineDest = outline.getAction();

			if (outlineDest != null) {
				bookmark = isHard
						? AppointmentUtils.asExplicitDestinationHard(outlineDest)
						: AppointmentUtils.asExplicitDestinationSoft(doc, outlineDest);
			}
		}

		if (bookmark != null) {
			log.debug("Appropriate level {} bookmark found referring to page {}",
					outline.getLevel(), bookmark.getPageNumber());
		}

		return bookmark;
	}

	/**
	 * Do NOT use this, has been replaced by {@link #extractExplicitDestinationHard(OutlineItemCollection)} in order to make
	 * intentions clearer, but {@link #extractExplicitDestinationSoft(IDocument, OutlineItemCollection)} should be used in
	 * most scenarios for more robust checking.
	 * @param outline The object to extract the destination from.
	 * @return The retrieved {@link ExplicitDestination} or null if it couldn't be casted or retrieved.
	 */
	@Deprecated
	public static ExplicitDestination extractExplicitDestination(OutlineItemCollection outline) {
		return extractExplicitDestinationHard(outline);
	}

	/**
	 * Moves a specific outline to a new parent.
	 * @param oldRoot The outline to move.
	 * @param newRoot The new parent to move the outline to.
	 */
	public static void moveOutline(OutlineItemCollection oldRoot, OutlineItemCollection newRoot) {
		Outlines oldParent = oldRoot.getParent();
		newRoot.add(oldRoot);
		oldParent.remove(oldRoot);
	}

	/**
	 * Do NOT use this, has been replaced by {@link #setDestinationOrActionHard(OutlineItemCollection, ExplicitDestination)}
	 * in order to make intentions clearer. Also see
	 * {@link #setDestinationOrActionSoft(Document, OutlineItemCollection, ExplicitDestination)} and the overloaded methods
	 * that accept a {@link NamedDestination}.
	 * @param outline The annotation to change.
	 * @param dest The destination to change to.
	 */
	@Deprecated
	public static void setDestinationOrAction(OutlineItemCollection outline, ExplicitDestination dest) {
		setDestinationOrActionHard(outline, dest);
	}

	/**
	 * Changes the underlying destination of an {@link OutlineItemCollection} to an {@link IAppointment}. Does NOT
	 * follow through wrapped {@link IAppointment}s if there are any in the outline, so this is useful if you want
	 * to change the destination of a single outline instead of the destination backed by a {@link NamedDestination} for
	 * example (which would then update the destinations of everything pointing to that specific name).
	 * @param outline The outline to change.
	 * @param dest The destination to change to.
	 * @param actionHandling What to do if dest is an action and there is already an action present on the outline.
	 */
	public static void setDestinationOrActionHard(OutlineItemCollection outline, IAppointment dest,
												  ActionHandling actionHandling) {
		// If the replacement is a PdfAction, set or add the action to the outline.
		if (dest instanceof PdfAction action) {
			Optional<IAppointment> unwrapped = AppointmentUtils.unwrapIfGoToAction(dest);
			if (unwrapped.isEmpty()) {
				if (outline.getAction() == null || actionHandling == ActionHandling.OVERWRITE) {
					outline.setAction(action);
				} else {
					outline.getAction().getNext().add(action);
				}
				return;
			}
			dest = unwrapped.get();
		}

		// In other cases we just replace the destination with the replacement.
		outline.setDestination(dest);
	}

	/**
	 * Changes the underlying destination of an {@link OutlineItemCollection} to an {@link ExplicitDestination}. Does
	 * NOT follow through wrapped {@link IAppointment}s if there are any, so this is useful if you want to change the
	 * destination of a single outline instead of the destination backed by a {@link NamedDestination} for example
	 * (which would then update the destinations of everything pointing to that specific name).
	 * @param outline The outline to change.
	 * @param dest The destination to change to.
	 */
	public static void setDestinationOrActionHard(OutlineItemCollection outline, ExplicitDestination dest) {
		// If there is already a destination set it. If there is no destination or action at all, set the destination
		// instead of the action as a sane default.
		if (outline.getDestination() != null || outline.getAction() == null) {
			outline.setDestination(dest);
		}

		if (outline.getAction() != null) {
			outline.setAction(new GoToAction(dest));
		}
	}

	/**
	 * Changes the underlying destination of an {@link OutlineItemCollection} to a {@link NamedDestination}. Does NOT
	 * follow through wrapped {@link IAppointment}s if there are any, so this is useful if you want to change the
	 * destination of a single outline instead of the destination backed by a {@link NamedDestination} for example
	 * (which would then update the destinations of everything pointing to that specific name).
	 * @param outline The outline to change.
	 * @param doc The document that contains the annotation.
	 * @param dest The destination to change to.
	 */
	public static void setDestinationOrActionHard(OutlineItemCollection outline, Document doc, NamedDestination dest) {
		// If there is already a destination set it. If there is no destination or action at all, set the destination
		// instead of the action as a sane default.
		if (outline.getDestination() != null || outline.getAction() == null) {
			outline.setDestination(dest);
		}

		if (outline.getAction() != null) {
			outline.setAction(new GoToAction(doc, dest.getName()));
		}
	}

	/**
	 * Changes the underlying destination of an {@link OutlineItemCollection} to an {@link ExplicitDestination}. DOES
	 * follow through wrapped {@link IAppointment}s if there are any, so this is useful if you want to or don't care
	 * about changing the destination of everything pointing to a {@link NamedDestination}.
	 * @param outline The outline to change.
	 * @param doc The document that contains the annotation.
	 * @param dest The destination to change to.
	 */
	public static void setDestinationOrActionSoft(Document doc, OutlineItemCollection outline,
												  ExplicitDestination dest) {
		boolean updated = false;
		if (outline.getDestination() != null) {
			outline.setDestination(AppointmentUtils.replaceIfWrapped(doc, outline.getDestination(), dest));
			updated = true;
		}

		if (outline.getAction() != null) {
			if (outline.getAction() instanceof GoToAction) {
				AppointmentUtils.replaceIfWrapped(doc, outline.getAction(), dest);
			} else {
				outline.setAction(new GoToAction(dest));
			}
			updated = true;
		}

		if (!updated) {
			outline.setDestination(dest);
		}
	}

	/**
	 * Changes the underlying destination of an {@link OutlineItemCollection} to a {@link NamedDestination}. DOES
	 * follow through wrapped {@link IAppointment}s if there are any, so this is useful if you want to or don't care
	 * about changing the destination of everything pointing to a {@link NamedDestination}.
	 * @param outline The outline to change.
	 * @param doc The document that contains the annotation.
	 * @param dest The destination to change to.
	 */
	public static void setDestinationOrActionSoft(Document doc, OutlineItemCollection outline,
												  NamedDestination dest) {
		boolean updated = false;
		if (outline.getDestination() != null) {
			outline.setDestination(AppointmentUtils.replaceIfWrapped(doc, outline.getDestination(), dest));
			updated = true;
		}

		if (outline.getAction() != null) {
			if (outline.getAction() instanceof GoToAction) {
				AppointmentUtils.replaceIfWrapped(doc, outline.getAction(), dest);
			} else {
				outline.setAction(new GoToAction(doc, dest.getName()));
			}
			updated = true;
		}

		if (!updated) {
			outline.setDestination(dest);
		}
	}

	/**
	 * Changes the underlying destination of an {@link OutlineItemCollection} to an {@link ExplicitDestination}
	 * pointing to the top left of a page. DOES follow through wrapped {@link IAppointment}s if there are any, so
	 * this is useful if you want to or don't care about changing the destination of everything pointing to a
	 * {@link NamedDestination}.
	 * @param outline The outline to change.
	 * @param doc The document that contains the annotation.
	 * @param target The target page to point to.
	 */
	public static XYZExplicitDestination setDestinationToUpperLeftCornerSoft(Document doc,
																			 OutlineItemCollection outline, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrActionSoft(doc, outline, dest);
		return dest;
	}

	/**
	 * Changes the underlying destination of an {@link OutlineItemCollection} to an {@link ExplicitDestination} pointing
	 * to the top left of a page. Does NOT follow through wrapped {@link IAppointment}s if there are any, so this is
	 * useful if you want to change the destination of a single outline instead of the destination backed by a
	 * {@link NamedDestination} for example (which would then update the destinations of everything pointing to that
	 * specific name).
	 * @param outline The outline to change.
	 * @param target The target page to point to.
	 */
	public static XYZExplicitDestination setDestinationToUpperLeftCornerHard(OutlineItemCollection outline, Page target) {
		XYZExplicitDestination dest = XYZExplicitDestination.createDestinationToUpperLeftCorner(target);
		setDestinationOrActionHard(outline, dest);
		return dest;
	}

	/**
	 * Do NOT use this, has been replaced by {@link #setDestinationToUpperLeftCornerHard(OutlineItemCollection, Page)}
	 * in order to make intentions clearer.
	 * @param outline The outline to change.
	 * @param target The target page to point to.
	 */
	@Deprecated
	public static XYZExplicitDestination setDestinationToUpperLeftCorner(OutlineItemCollection outline, Page target) {
		return setDestinationToUpperLeftCornerHard(outline, target);
	}
}
