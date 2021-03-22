package com.docshifter.core.work;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;

import com.docshifter.core.monitoring.services.AbstractServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class TestWorkfolder extends AbstractServiceTest {

	@Autowired
	WorkFolderManager manager;
	WorkFolder workfolder;



	@Test
	public void testCreateWorkfolder() {
		try {
			workfolder = manager.getNewWorkfolder("testcreate");
		} catch (IOException e) {
			fail("Exception: " + e);
		}
		File folder = new File(workfolder.toString());
		assertTrue(folder.exists());
	}
	
	@Test
	public void testDeleteWorkfolder() {
		try {
			workfolder = manager.getNewWorkfolder("testdelete");
		} catch (IOException e) {
			fail("Exception: " + e);
		}
		File folder = new File(workfolder.toString());
		manager.deleteWorkfolder(workfolder);
		assertFalse(folder.exists());
	}
}
