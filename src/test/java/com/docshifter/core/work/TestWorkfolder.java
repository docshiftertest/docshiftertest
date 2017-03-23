package com.docshifter.core.work;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;

import com.docshifter.core.TestController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.docshifter.core.work.WorkFolder;
import com.docshifter.core.work.WorkFolderManager;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestController.class)
public class TestWorkfolder {
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
	
	@After
	public void after() {
		File folder = new File(workfolder.toString());
		if (folder.exists()) {
			manager.deleteWorkfolder(workfolder);
		}
	}

}
