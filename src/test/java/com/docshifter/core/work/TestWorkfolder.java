package com.docshifter.core.work;


import com.docshifter.core.monitoring.services.AbstractServiceTest;
import com.google.common.io.Files;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


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

	/**
	 * Tests if the workfFolder was copied
	 * Tests if the new workFolder has the same files as the old workFolder
	 */
	@Test
	public void testCopyWorkFolder() {
		String newPath = null;
		File file = null;
		try {
			WorkFolder folder = manager.getNewWorkfolder("test");
			file = new File(folder.getFolder().toString() + File.separator + "test.docx");

			// the file can't be null
			assertNotNull(file);

			Files.touch(file);

			// the file must exist
			assertTrue(file.exists());

			newPath = manager.copyWorkFolder(manager.getNewWorkfolder("temp-" + "test"), folder);
		} catch (IOException e) {
			fail("Exception: " + e);
		}
		File folder = new File(newPath);

		assertNotNull(folder);
		assertTrue(folder.exists());
		assertTrue(Objects.requireNonNull(folder.listFiles()).length > 0);
		assertEquals(Objects.requireNonNull(folder.listFiles()).length, Objects.requireNonNull(file.listFiles()).length);
	}
}
