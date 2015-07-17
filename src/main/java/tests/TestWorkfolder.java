package tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.docbyte.docshifter.work.WorkFolder;
import com.docbyte.docshifter.work.WorkFolderManager;

public class TestWorkfolder {	
	WorkFolderManager manager;
	WorkFolder workfolder;
	
	@Before
	public void before() {
		try {
			manager = WorkFolderManager.getInstance();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateWorkfolder() {
		try {
			workfolder = manager.getNewWorkfolder("testcreate");
		} catch (IOException e) {
			Assert.fail("Exception: " + e);
		}
		File folder = new File(workfolder.toString());
		assertTrue(folder.exists());
	}
	
	@Test
	public void testDeleteWorkfolder() {
		try {
			workfolder = manager.getNewWorkfolder("testdelete");
		} catch (IOException e) {
			Assert.fail("Exception: " + e);
		}
		File folder = new File(workfolder.toString());
		manager.deleteWorkfolder(workfolder);
		assertFalse(folder.exists());
	}
	
	@After
	public void after() {
		File folder = new File(workfolder.toString());
		if (folder.exists()) {
			manager.deleteWorkfolder(workfolder);
		}
	}

}
