package com.docshifter.core.dctm;

import com.docshifter.core.utils.dctm.DctmConnectionDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DctmConnectionDetailsTest {
	@Test
	public void fromProperties() throws Exception {
		
		DctmConnectionDetails details = DctmConnectionDetails.fromProperties("connDetailTest.properties");
		
		Assertions.assertEquals("ThisIsTheRepo", details.getRepository());
		Assertions.assertEquals("ThisIsTheUsername", details.getUsername());
		Assertions.assertEquals("ThisIsThePassword", details.getPassword());
		
		
	}
	
}
