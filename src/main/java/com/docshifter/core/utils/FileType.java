package com.docshifter.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum FileType {
	Pdf(new String[]{"pdf"}),
	Word(new String[]{"docx", "doc", "docm", "dot", "dotx", "dotm", "docb"}),
	Excel(new String[]{"xslx", "xls", "xlsm", "xlm", "xltx", "xlt", "xltm", "xlsb"}),
	PowerPoint(new String[]{"pptx", "ppt", "ppsx", "pps", "pptm", "ppsm", "potx", "potm", "sldx", "sldm"});

	private final Set<String> knownExtensions;

	FileType(String[] knownExtensions) {
		this.knownExtensions = Collections.unmodifiableSet(
				(Set<? extends String>) Arrays.stream(knownExtensions)
						.collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	/**
	 * A set of al known extensions associated with this file type. Set elements are mostly ordered from most to least
	 * prevalent extensions.
	 */
	public Set<String> getKnownExtensions() {
		return knownExtensions;
	}
}
