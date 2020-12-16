package com.docshifter.core.asposehelper;

import com.docshifter.core.asposehelper.LicenseHelper;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class LicenseHelperTest {

	@Test
	public void testLicenseHelp() {
		LicenseHelper helper = LicenseHelper.getLicenseHelper();
		assertNotNull(helper);
	}
}
