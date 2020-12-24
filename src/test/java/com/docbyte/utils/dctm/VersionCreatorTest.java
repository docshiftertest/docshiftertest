package com.docbyte.utils.dctm;

import com.docshifter.core.utils.dctm.VersionCreator;
import org.junit.Ignore;
import org.junit.Test;

public class VersionCreatorTest {
	
	@Ignore
	@Test
	public void createVersions() throws Exception {
		VersionCreator.createVersions("Optimco_dev", "dev.admin", "Doc_Byte", "090000018004a517", 1000);
	}
}
