package com.docshifter.core.work;


import com.docshifter.core.monitoring.services.AbstractServiceTest;
import com.google.common.io.Files;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


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
		Path sourcePath = null;
		Path targetPath = null;
		File file = null;
		WorkFolder folder = null;
		try {
			folder = manager.getNewWorkfolder("test");
			sourcePath = folder.getFolder();
			file = new File(Paths.get(folder.getFolder().toString(), "test.docx").toString());

			// the file can't be null
			assertNotNull(file);

			Files.touch(file);

			// the file must exist
			assertTrue(file.exists());

			targetPath = manager.copyWorkFolder(folder, manager.getNewWorkfolder("temp-" + "test"));
		}
		catch (IOException e) {
			fail("Exception: " + e);
		}
		assertNotNull(sourcePath);
		File sourceFolder = sourcePath.toFile();
		assertTrue(sourceFolder.exists());
		assertTrue(sourceFolder.isDirectory());
		assertTrue(Objects.requireNonNull(sourceFolder.listFiles()).length > 0);
		assertNotNull(targetPath);
		File targetFolder = targetPath.toFile();
		assertTrue(targetFolder.exists());
		assertTrue(targetFolder.isDirectory());
		assertTrue(Objects.requireNonNull(targetFolder.listFiles()).length > 0);
		assertEquals(Objects.requireNonNull(sourceFolder.listFiles()).length,
				Objects.requireNonNull(targetFolder.listFiles()).length);
	}
}
