package com.docshifter.core.dctm;

import com.docshifter.core.utils.dctm.VersionCreator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class VersionCreatorTest {
	
	@Disabled
	@Test
	public void createVersions() throws Exception {
		VersionCreator.createVersions("Optimco_dev", "dev.admin", "Doc_Byte", "090000018004a517", 1000);
	}
}
