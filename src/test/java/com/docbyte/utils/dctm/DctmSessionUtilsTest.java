package com.docbyte.utils.dctm;

import com.docshifter.core.utils.dctm.DctmConnectionDetails;
import com.docshifter.core.utils.dctm.DctmSession;
import com.docshifter.core.utils.dctm.DctmSessionUtils;
import org.junit.Assert;
import org.junit.Test;

public class DctmSessionUtilsTest {
	@Test
	public void createSession() throws Exception {
		DctmSession session = null;
		
		try {
			session = DctmSessionUtils.getInstance().createSession(DctmConnectionDetails.fromProperties("repoTest.properties"));
			
			Assert.assertNotNull(session);
			Assert.assertTrue(session.isConnected());
		} finally {
			if (session != null)
				session.close();
		}
		
		Assert.assertNotNull(session);
		Assert.assertFalse(session.isConnected());
	}
}
