package com.docshifter.core.asposehelper;

import com.docshifter.core.asposehelper.AsposeVersionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AsposeVersionUtilTest {

	@Test
	public void testVersions() {
		String actualVersion;
		String pomVersion;
		String[] testClasses = new String[] {"eMail", "slides", "Cells", "words", "PDF", 
				"pdf", "Pdf", "imaging", "taSKs",
				//"ocr",
				"diagram", "cad"};
		for (String testClass : testClasses) {
			actualVersion = AsposeVersionUtil.getActualVersion(testClass);
			pomVersion = AsposeVersionUtil.getVersionFromPom(testClass);
			assertNotNull(actualVersion);
			assertNotNull(pomVersion);
		}
	}
}
