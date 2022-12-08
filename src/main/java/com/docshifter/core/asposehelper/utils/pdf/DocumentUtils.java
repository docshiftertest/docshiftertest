package com.docshifter.core.asposehelper.utils.pdf;

import com.aspose.pdf.Document;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Utility methods for Aspose.PDF {@link Document Documents}.
 */
@Log4j2
public final class DocumentUtils {
	private DocumentUtils() {}

	/**
	 * Closes and reopens a {@link Document}. The {@code doc} that was passed will be disposed of and will therefore
	 * no longer be safe to use! It is strongly recommended to simply reassign that variable to the object that is
	 * returned from this method.
	 * @param doc The {@link Document} to reload.
	 * @return A reloaded {@link Document}.
	 * @throws IOException Something went wrong while trying to pass the {@link Document}'s data through piped streams.
	 * @throws RuntimeException Something went wrong while trying to save/open the {@link Document}.
	 */
	public static Document reload(Document doc) throws IOException {
		long start = System.currentTimeMillis();
		try (doc;
			 PipedInputStream pis = new PipedInputStream();
			 PipedOutputStream pos = new PipedOutputStream(pis)) {
			new Thread(() -> {
				try {
					doc.save(pos);
				} catch (Exception ex) {
					log.error("Unable to reload document, save operation failed", ex);
				} finally {
					try {
						pos.close();
					} catch (Exception ex) {
						log.error("Ran into an exception while trying to close piped stream during reloading of " +
								"document",	ex);
					}
				}
			}).start();
			return new Document(pis);
		} finally {
			log.debug("Reloading the document took {} ms", System.currentTimeMillis() - start);
		}
	}
}
