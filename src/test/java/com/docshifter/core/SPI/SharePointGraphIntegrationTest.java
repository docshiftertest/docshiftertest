package com.docshifter.core.SPI;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.docbyte.utils.FileUtils;
import com.docshifter.core.graphAPI.GraphClient;
import com.docshifter.core.graphAPI.MSGraphAuthenticationBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.FieldValueSet;
import com.microsoft.graph.models.extensions.Folder;
import com.microsoft.graph.models.extensions.ListItem;
import com.microsoft.graph.requests.extensions.IListCollectionPage;

/**
 * @author Juan Marques created on 07/08/2020
 */
@RunWith(JUnit4.class)
public class SharePointGraphIntegrationTest {

	private final Logger log = Logger.getLogger(SharePointGraphIntegrationTest.class);

	private GraphClient graphClient;
	
	private static String site = "demo";

	@Before
	public void before() {

		graphClient = new GraphClient(MSGraphAuthenticationBuilder.createGraphClient(
				"c01820bd-f5ab-4a94-8fcc-1c47d6b264f5", "Cf12Tr0~7P7D.Kof0_Rb6k81_bZ9J8Cl6k",
				"fdfaf67a-261f-4fc8-bc26-fa14aa7691e1", NationalCloud.Global));

		if (site.equalsIgnoreCase("demo"))
			site = graphClient.getSharepoint().retrieveSiteId(site);
	}

	@Test
	public void badCredentialsTest() {

		graphClient = new GraphClient(MSGraphAuthenticationBuilder.createGraphClient("b081-42d7-a8e1-4c93452d9a3c",
				"3O1p8_T2XatR-PCRd18ywH~DU_tEx.m433", "a545304d-99b4-4706-8c12-f626a2d2a3cb", NationalCloud.Global));

		IListCollectionPage collectionPage = null;

		try {
			collectionPage = graphClient.getSharepoint().getLibrary(site);
		} catch (ClientException e) {
			log.error("ohh noo" + e.getMessage());
		}
		assertNull(collectionPage);
	}


	@Test
	public void getAllContentFromSpecificFolderTest() {

		String folderName = "Test";
		String listId = "48aa2e0d-0599-4be3-976d-4296f601ac34";

		List<DriveItem> items = new ArrayList<>();
		graphClient.getSharepoint().getAllItemDriveCollectionPage(items, graphClient.getSharepoint().getAllDriveItems(listId, site), folderName);

		DriveItem item = items.stream().findFirst().orElseThrow(NoSuchElementException::new);

		getAllFiles(listId, item.id);
	}

	public void getAllFiles(String listId, String driveItemId) {

		List<DriveItem> lstChildItems = new ArrayList<>();
		graphClient.getSharepoint().getAllItemDriveCollectionPage(lstChildItems,
				graphClient.getSharepoint().getAllContentFromSpecificFolder(listId, driveItemId, site), StringUtils.EMPTY);

		for (DriveItem childFolderItems : lstChildItems) {
			if (childFolderItems.file != null) {
				JsonObject listItem = childFolderItems.getRawObject().getAsJsonObject("listItem");
				JsonElement fieldsElement = listItem.get("fields");
				Map<String, String> fields = jsonToMap(fieldsElement);
				boolean processedByDS = Boolean.parseBoolean(fields.getOrDefault("ProcessedByDS", "false"));
				if (!processedByDS) {

					// Updating field
					updateFields(listId, fields.get("id"), graphClient,true);
					
					InputStream fileInputStream = graphClient.getSharepoint().getFileByDriveId(childFolderItems.id, site,listId);

					downloadFile(fileInputStream, childFolderItems.name);
					
         			//Updating field to false to get the file again
					updateFields(listId, fields.get("id"), graphClient,false);
				}
			}

			else if (childFolderItems.folder.childCount > 0) {
				getAllFiles(listId, childFolderItems.id);
			}
		}

	}
	
	/**
	 * Update column
	 * 
	 * @param listId
	 * @param itemId
	 * @param graphClient.getSharepoint()
	 */
	public void updateFields(String listId, String itemId, GraphClient graphClient,boolean processed) {
		FieldValueSet fieldValueSet = new FieldValueSet();
		fieldValueSet.additionalDataManager().put("ProcessedByDS", new JsonPrimitive(processed));

		graphClient.getSharepoint().updateFields(listId, itemId, fieldValueSet, site);

	}

	public void downloadFile(InputStream in, String fileName) {

		File file = new File("./target/test-classes/ds/work/" + fileName);

		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			IOUtils.copy(in, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getAllLists() {

		List<ListItem> allItems = new ArrayList<>();

		graphClient.getSharepoint().getLibrary(site).getCurrentPage().forEach(c -> {

			if (c.name.equalsIgnoreCase("InputDev")) {
				log.info(c.id);
				log.info(c.name);
				log.info("    \n");
				graphClient.getSharepoint().getLibraryItems(c.id, allItems, "Document", site);
			}
		});

		allItems.forEach(r -> {

			log.info(r.webUrl);
			log.info(r.id);
			log.info("parentReference.driveId: " + r.parentReference.driveId);
			log.info("parentReference.id: " + r.parentReference.id);
			log.info("parentReference.name : " + r.parentReference.name);
			log.info("name : " + r.name);

			String weburl = r.webUrl;
			log.info("Actual url..." + weburl);
			weburl = getFolderPath(weburl);
			log.info("Folder Path..." + weburl);
		});
	}

	@Test
	public void getCurrentPathTest() {
		String weburl = "https://docshifterdev.sharepoint.com/Shared%20Documents/Output/watermark/26_Nonclinical_Summary.pdf";

		log.info("Actual url..." + weburl);
		weburl = getFolderPath(weburl);
		log.info("Folder Path..." + weburl);

		assertNotEquals(StringUtils.EMPTY, weburl);
	}

	public String getFolderPath(String webURL) {

		URL url = null;
		try {
			url = new URL(webURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (url != null) {

			webURL = url.getPath();
			return FileUtils.getNameWithoutExtension(webURL.substring(StringUtils.ordinalIndexOf(webURL, "/", 2)));
		}

		return StringUtils.EMPTY;
	}

	@Test
	@Ignore
	public void createFolderTest() {

		DriveItem driveItem = new DriveItem();
		driveItem.name = "NewChildrenlv3";
		Folder folder = new Folder();
		driveItem.folder = folder;
		driveItem.additionalDataManager().put("@microsoft.graph.conflictBehavior", new JsonPrimitive("rename"));

		DriveItem driveItemParentCallBack = null;
		DriveItem driveItemChildCallBack = null;

		try {
			driveItemParentCallBack = graphClient.getSharepoint().createRootFolder("fd4b0a4f-4ab3-4ca3-9bdb-7addbc3e5ee0", driveItem, site);

			driveItemChildCallBack = graphClient.getSharepoint().createChildrenFolder("fd4b0a4f-4ab3-4ca3-9bdb-7addbc3e5ee0",
					driveItemParentCallBack.name, driveItem, site);
		} catch (ClientException ex) {
			log.info(ex);
		}

		assertNotNull(driveItemParentCallBack);
		assertNotNull(driveItemChildCallBack);

	}

	public Map<String, String> jsonToMap(JsonElement jsonElement) {
		Gson gson = new Gson();

		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		return gson.fromJson(jsonElement, type);
	}

}