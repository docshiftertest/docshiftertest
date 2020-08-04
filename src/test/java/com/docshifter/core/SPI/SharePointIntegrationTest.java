package com.docshifter.core.SPI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.Resource;

import com.docshifter.core.sharepointConnection.SharePointClient;

/**
 * @author Juan Marques created on 30/07/2020
 */
@RunWith(JUnit4.class)
public class SharePointIntegrationTest {

	private final Logger log = Logger.getLogger(SharePointIntegrationTest.class);

	private SharePointClient cli;

	// TODO - Move credentials to application properties and replace to our own SP
	// server.
	@Before
	public void before() {
		cli = SharePointClient.createSharePointClient("docshifter@docshifterdev.onmicrosoft.com", "3iGAq609LhrW",
				"docshifterdev.sharepoint.com", "");
	}

	@Test
	public void testConnection() throws Exception {
		log.info("Running testConnection()");
		assertNotNull(cli.getHeaderHelper());
	}

	@Test
	public void downloadFileTest() throws Exception {
		log.info("Running downloadFileTest()");
		Resource r = cli.downloadFile("/Shared Documents/docshifter-62-installation-guide.pdf");

		try (InputStream inputStream = r.getInputStream()) {
			File file = new File("./target/test-classes/ds/work/docshifter-62-installation-guide.pdf");

			try (FileOutputStream outputStream = new FileOutputStream(file)) {
				IOUtils.copy(inputStream, outputStream);
			}
			assertTrue(file.exists());
		}
	}

	@Test
	public void moveFileTest() throws Exception {
		log.info("Running moveFileTest()");

		JSONObject movingTo = cli.moveFile("/Shared Documents/docshifter-62-installation-guide.pdf",
				"Shared Documents/eCTD Content/docshifter-62-installation-guide.pdf", 1);

		assertEquals(200, movingTo.get("statusCodeValue"));

		JSONObject movingBack = cli.moveFile("/Shared Documents/eCTD Content/docshifter-62-installation-guide.pdf",
				"/Shared Documents/docshifter-62-installation-guide.pdf", 1);

		assertEquals(200, movingBack.get("statusCodeValue"));
	}

	@Test
	public void updateFileMetadataTest() throws JSONException, Exception {

		String folder = "/Shared Documents";

		JSONObject allFiles = cli.getAllFilesFromFolder(folder);

		JSONArray jArray = allFiles.getJSONObject("d").getJSONArray("results");

		for (Object json : jArray) {

			String relativeUrl = ((JSONObject) json).getString("ServerRelativeUrl");

			log.info("File: " + relativeUrl);

			JSONObject update = cli.updateFileMetadata(relativeUrl, new JSONObject("{ProcessedByDS: true}"));

			assertEquals(204, update.get("statusCodeValue"));

		}

	}

	@Test
	public void downloadAllFilesFromFolderTest() throws Exception {
		log.info("Running downloadAllFilesFromFolderTest()");
		String folder = "/Shared Documents";

		ArrayList<String> files = new ArrayList<>();

		JSONObject allFiles = cli.getAllFilesFromFolder(folder);

		JSONArray jArray = allFiles.getJSONObject("d").getJSONArray("results");

		for (Object json : jArray) {

			files.add(((JSONObject) json).getString("Name"));

		}

		for (String file : files) {

			Resource r = null;
			try {
				r = cli.downloadFile(folder + "/" + file);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try (InputStream inputStream = r.getInputStream()) {
				File newFile = new File("./target/test-classes/ds/work/" + file);

				try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
					IOUtils.copy(inputStream, outputStream);
				}
				assertTrue(newFile.exists());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Test
	public void getFilePropertiesTest() throws Exception {
		log.info(cli.getFilesProperties("/Shared Documents", "/docshifter-62-installation-guide.pdf"));
	}

	@Test
	public void uploadFileAndUpdateMetaDataTest() throws Exception {
		log.info("Running uploadFileTest()");

		Resource r = cli.downloadFile("/Shared Documents/docshifter-62-installation-guide.pdf");
		cli.uploadFileAndUpdateMetaData("/Shared Documents", r, new JSONObject("{ProcessedByDS: false}"), true,
				"docshifter-62-installation-guide.docx");
	}

	@Test
	public void updateFileTest() throws Exception {
		Resource r = cli.downloadFile("/Shared Documents/docshifter-62-installation-guide.pdf");
		cli.uploadFile("/Shared Documents/Output", r, true, "docshifter-62-installation-guide.docx");
	}

	@Test
	@Ignore
	public void createFolderTest() throws Exception {
		cli.createFolder("/Shared Documents", "Output", null);

	}

}
