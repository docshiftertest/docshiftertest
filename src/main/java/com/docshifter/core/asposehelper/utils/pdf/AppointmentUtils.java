package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.ExplicitDestination;
import com.aspose.pdf.GoToAction;
import com.aspose.pdf.IAppointment;
import com.aspose.pdf.IDocument;
import com.aspose.pdf.NamedDestination;
import lombok.extern.log4j.Log4j2;

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

		if (appointment instanceof GoToAction) {
			IAppointment dest = ((GoToAction) appointment).getDestination();
			if (dest != null) {
				return unwrap(doc, ((GoToAction) appointment).getDestination());
			}
		}

		return appointment;
	}

	/**
	 * Unwraps {@link GoToAction}s from an {@link IAppointment} so we get the bare {@link IAppointment} under it. ONLY use
	 * this if you don't care about following {@link NamedDestination}s! For more robust checking see {@link #unwrap(IDocument, IAppointment)}.
	 * @param appointment The object to unwrap.
	 * @return The most inner {@link IAppointment}.
	 */
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
