package com.docbyte.utils.dctm;

import com.docshifter.core.utils.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.fail;

/**
 * Created by pieter.vaneeckhout on 6/19/2017.
 */
public class FileUtilsTest {

	@Test
	public void testReplaceInvalidChars(){

		String filename = "test \\  ....... File | name .. for /* will . this : \" be <valid> \t\t?";

		filename = FileUtils.removeIllegalFilesystemCharacters(filename);

		assertThat(filename, not(containsString(String.valueOf('\u0016'))));

		try {
			testCreateFile(filename);
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testRemoveControlCaharacters() {
		String filename = "GROUPE 503710 - TR: MARTE/VIP 503.710  Wei-Tji bvba(Aspose.Email Evaluation)";

		filename = FileUtils.removeIllegalFilesystemCharacters(filename);

		assertThat(filename, not(containsString(String.valueOf('\u0016'))));

		try {
			testCreateFile(filename);
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testWindowsReservedFilenames() {
		if (SystemUtils.IS_OS_WINDOWS) {
			String[] reservedFileNames = new String[]{"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", ".", ".."};
			
			for (String filename : reservedFileNames) {
				filename = FileUtils.removeIllegalFilesystemCharacters(filename);
				
				assertThat(Arrays.asList(reservedFileNames), not(hasItem(filename)));
				
				try {
					testCreateFile(filename);
				} catch (Exception e) {
					fail();
				}
			}
		}
	}

	@Test
	public void testJapaneseFileName() {
		String filename = "テストファイル名";

		filename = FileUtils.shortenFileName(filename);
		filename = FileUtils.removeIllegalFilesystemCharacters(filename);

		try {
			testCreateFile(filename);
		} catch (Exception e) {
			fail();
		}
	}

	private void testCreateFile(String filename) throws IOException {
		Files.write(Paths.get("target/test-classes/" + filename), new byte[]{}, StandardOpenOption.CREATE, StandardOpenOption.DELETE_ON_CLOSE);
	}
}
