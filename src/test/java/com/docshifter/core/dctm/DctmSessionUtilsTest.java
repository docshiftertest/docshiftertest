package com.docshifter.core.dctm;

import com.docshifter.core.utils.dctm.DctmConnectionDetails;
import com.docshifter.core.utils.dctm.DctmSession;
import com.docshifter.core.utils.dctm.DctmSessionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DctmSessionUtilsTest {
	@Disabled("There's no Dctm repo to talk to right now")
	@Test
	public void createSession() throws Exception {
		DctmSession session = null;
		
		try {
			session = DctmSessionUtils.getInstance().createSession(DctmConnectionDetails.fromProperties("repoTest.properties"));
			
			Assertions.assertNotNull(session);
			Assertions.assertTrue(session.isConnected());
		} finally {
			if (session != null)
				session.close();
		}
		
		Assertions.assertNotNull(session);
		Assertions.assertFalse(session.isConnected());
	}
}
