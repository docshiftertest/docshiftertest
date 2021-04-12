package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.Document;
import com.aspose.pdf.ExplicitDestination;
import com.aspose.pdf.GoToAction;
import com.aspose.pdf.IAppointment;
import com.aspose.pdf.IDocument;
import com.aspose.pdf.NamedDestination;
import com.aspose.pdf.PdfAction;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Log4j2
public final class AppointmentUtils {
	private AppointmentUtils() {}

	/**
	 * Unwraps an {@link IAppointment} entirely and tries to cast the result as an {@link ExplicitDestination}. Will
	 * return null if it is not an {@link ExplicitDestination}.
	 * @param doc The document containing the {@link IAppointment} and {@link NamedDestination}s.
	 * @param appointment The object to cast.
	 * @return The casted {@link ExplicitDestination} or null if it couldn't be casted.
	 */
	public static ExplicitDestination asExplicitDestinationSoft(IDocument doc, IAppointment appointment) {
		appointment = unwrap(doc, appointment);

		// Check if destination is of right type
		if (!(appointment instanceof ExplicitDestination)) {
			if (appointment != null) {
				log.debug("{} was not a ExplicitDestination: skipping. It's a {}",
						appointment, appointment.getClass().getName());
			}
			return null;
		}

		return (ExplicitDestination)appointment;
	}

	/**
	 * Unwraps {@link GoToAction}s from an {@link IAppointment} entirely and tries to cast the result as an
	 * {@link ExplicitDestination}. Will return null if it is not an {@link ExplicitDestination}. ONLY use
	 * this if you don't care about following {@link NamedDestination}s! For more robust checking see
	 * {@link #asExplicitDestinationSoft(IDocument, IAppointment)}.
	 * @param appointment The object to cast.
	 * @return The casted {@link ExplicitDestination} or null if it couldn't be casted.
	 */
	public static ExplicitDestination asExplicitDestinationHard(IAppointment appointment) {
		// It might be a GoToAction wrapping an ExplicitDestination...
		appointment = unwrapGoToAction(appointment);

		// Check if destination is of right type
		if (!(appointment instanceof ExplicitDestination)) {
			if (appointment != null) {
				log.debug("{} was not a ExplicitDestination: skipping. It's a {}",
						appointment, appointment.getClass().getName());
			}
			return null;
		}

		return (ExplicitDestination)appointment;
	}

	/**
	 * Unwraps an {@link IAppointment} entirely. This means following {@link NamedDestination}s and {@link GoToAction}s until no
	 * more references can be followed.
	 * @param doc The document containing the {@link IAppointment} and {@link NamedDestination}s.
	 * @param appointment The object to unwrap.
	 * @return The most inner {@link IAppointment}.
	 */
	public static IAppointment unwrap(IDocument doc, IAppointment appointment) {
		if (appointment instanceof NamedDestination) {
			return unwrap(doc, doc.getNamedDestinations().get_Item(((NamedDestination)appointment).getName()));
		}

		Optional<GoToAction> goToAction = getIfGoToAction(appointment);
		if (goToAction.isPresent()) {
			return unwrap(doc, goToAction.get().getDestination());
		}

		return appointment;
	}

	// TODO: was meant to be replaced by getIfGoToAction but ran into regressions in HiFi, so investigate why!
	private static IAppointment unwrapGoToAction(IAppointment appointment) {
		if (appointment instanceof GoToAction) {
			IAppointment dest = ((GoToAction) appointment).getDestination();
			if (dest != null) {
				return unwrapGoToAction(((GoToAction) appointment).getDestination());
			}
		}

		return appointment;
	}

	/**
	 * Checks if the {@link IAppointment} is a {@link GoToAction} or a {@link PdfAction} that has a
	 * {@link GoToAction} somewhere next to it.
	 * @param appointment The {@link IAppointment} to check.
	 * @return The {@link GoToAction}.
	 */
	public static Optional<GoToAction> getIfGoToAction(IAppointment appointment) {
		if (appointment instanceof PdfAction) {
			return expandActions((PdfAction) appointment)
					.filter(action -> action instanceof GoToAction)
					.map(action -> (GoToAction) action)
					.filter(action -> action.getDestination() != null)
					.findFirst();
		}

		return Optional.empty();
	}

	/**
	 * Gets a stream of a {@link PdfAction} and all actions next to it.
	 * @param action The action to start from.
	 * @return A stream of {@link PdfAction}.
	 */
	public static Stream<PdfAction> expandActions(PdfAction action) {
		return Stream.concat(Stream.of(action), StreamSupport.stream(action.getNext().spliterator(), false));
	}

	/**
	 * Unwraps a {@link GoToAction} so we get the bare {@link IAppointment} under it.
	 * This is either a {@link NamedDestination} or a {@link ExplicitDestination}.
	 * @param appointment The object to unwrap.
	 * @return The wrapped {@link IAppointment} or an empty {@link Optional} if the appointment was not a
	 * {@link GoToAction}.
	 */
	public static Optional<IAppointment> unwrapIfGoToAction(IAppointment appointment) {
		return getIfGoToAction(appointment).map(GoToAction::getDestination);
	}

	/**
	 * Wraps an {@link IAppointment} inside a {@link GoToAction} if possible and returns it. If the appointment is
	 * already a {@link PdfAction}, it is simply casted and returned.
	 * @param doc The document that contains the appointment.
	 * @param appointment The object to wrap (or to check if it's already a {@link PdfAction}.
	 * @return A wrapped {@link GoToAction} or the casted {@link PdfAction}.
	 * @throws IllegalArgumentException If the {@link IAppointment} is not an existing {@link PdfAction},
	 * {@link ExplicitDestination} or {@link NamedDestination} and can therefore not be wrapped. This should in
	 * theory never occur unless a custom class implementing {@link IAppointment} has been created and supplied.
	 */
	public static PdfAction wrapPdfAction(Document doc, IAppointment appointment) {
		if (appointment instanceof ExplicitDestination) {
			return new GoToAction((ExplicitDestination) appointment);
		}

		if (appointment instanceof NamedDestination) {
			return new GoToAction(doc, ((NamedDestination)appointment).getName());
		}

		if (appointment instanceof PdfAction) {
			return (PdfAction) appointment;
		}

		throw new IllegalArgumentException("Expected an existing PdfAction, ExplicitDestination or NamedDestination." +
				" Got a " + appointment.getClass().getName() + " instead.");
	}

	/**
	 * Checks if an {@link IAppointment} is wrapped inside a {@link NamedDestination} or {@link GoToAction}.
	 * @param appointment The object to check.
	 * @return Whether the object is wrapping another {@link IAppointment}.
	 */
	public static boolean isWrapper(IAppointment appointment) {
		if (appointment instanceof NamedDestination) {
			return true;
		}

		if (appointment instanceof GoToAction) {
			return ((GoToAction)appointment).getDestination() != null;
		}

		return false;
	}

	/**
	 * Checks if an {@link IAppointment} is wrapped inside a {@link NamedDestination} or {@link GoToAction} and replaces the most
	 * inner {@link IAppointment} with a specified replacement.
	 * @param doc The document containing the {@link IAppointment} and {@link NamedDestination}s.
	 * @param source The source to start checking from.
	 * @param replacement The replacement value.
	 * @return The replacement if the source is not wrapping something, otherwise the source.
	 */
	public static IAppointment replaceIfWrapped(IDocument doc, IAppointment source, IAppointment replacement) {
		if (!isWrapper(source)) {
			return replacement;
		}

		replaceWrapped(doc, source, replacement);
		return source;
	}

	private static void replaceWrapped(IDocument doc, IAppointment source, IAppointment replacement) {
		if (source instanceof NamedDestination) {
			String destName = ((NamedDestination)source).getName();
			IAppointment wrapped = doc.getNamedDestinations().get_Item(destName);
			if (isWrapper(wrapped)) {
				replaceWrapped(doc, wrapped, replacement);
				return;
			}
			doc.getNamedDestinations().set_Item(destName, replacement);
			return;
		}

		if (source instanceof GoToAction) {
			IAppointment wrapped = ((GoToAction) source).getDestination();
			if (isWrapper(wrapped)) {
				replaceWrapped(doc, wrapped, replacement);
				return;
			}
			((GoToAction) source).setDestination(replacement);
		}
	}
}
