package com.docbyte.utils.dctm;

import com.docshifter.core.utils.dctm.DctmConnectionDetails;
import org.junit.Assert;
import org.junit.Test;

public class DctmConnectionDetailsTest {
	@Test
	public void fromProperties() throws Exception {
		
		DctmConnectionDetails details = DctmConnectionDetails.fromProperties("connDetailTest.properties");
		
		Assert.assertEquals("ThisIsTheRepo", details.getRepository());
		Assert.assertEquals("ThisIsTheUsername", details.getUsername());
		Assert.assertEquals("ThisIsThePassword", details.getPassword());
		
		
	}
	
}