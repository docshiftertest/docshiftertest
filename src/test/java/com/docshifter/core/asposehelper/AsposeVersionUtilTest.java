package com.docshifter.core.asposehelper;

import com.docshifter.core.asposehelper.AsposeVersionUtil;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

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
