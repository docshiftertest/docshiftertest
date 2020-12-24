package com.docshifter.core.utils.dctm;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.operations.IDfFormatRecognizer;

import java.nio.file.Path;

/**
 * Created by samnang.nop on 31/03/2017.
 */
public class FormatRecognizer {

	public static String detectFileFormat(Path inFilePath, DfClientX clientX, IDfSession session) throws DfException {
		IDfFormatRecognizer recognizer = clientX.getFormatRecognizer(session, inFilePath.toString(), null);
		return recognizer.getDefaultSuggestedFileFormat();

	}
}
