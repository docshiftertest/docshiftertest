package com.docshifter.core.SPI;

import com.docshifter.core.graphAPI.GraphClient;
import com.docshifter.core.graphAPI.MSGraphAuthenticationBuilder;
import com.docshifter.core.utils.FileUtils;
import com.google.gson.JsonPrimitive;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.FieldValueSet;
import com.microsoft.graph.models.ListItem;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.serializer.AdditionalDataManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
/**
 * @author Juan Marques created on 07/08/2020
 */
@Log4j2
public class SharePointGraphIntegrationTest {

	private static GraphClient graphClient;
	
	private static final String site = "demo";

	@BeforeAll
	public static void before() throws ExecutionException, InterruptedException {

		graphClient = new GraphClient(MSGraphAuthenticationBuilder.createGraphClient(
				"c01820bd-f5ab-4a94-8fcc-1c47d6b264f5", "Cf12Tr0~7P7D.Kof0_Rb6k81_bZ9J8Cl6k",
				"fdfaf67a-261f-4fc8-bc26-fa14aa7691e1"));
	}

	@Test
	void processFullList() throws ExecutionException, InterruptedException {

		String siteId = graphClient.getSharepoint().getSiteId(site);

		String listName = "Test Input";
		List<DriveItem> driveItemList = new ArrayList<>();

		String driveListId = graphClient.getSharepoint().getDriveListIdByListName(siteId,listName);

		String listId = graphClient.getSharepoint().getListIdByDriveId(siteId,driveListId);

		DriveItemCollectionPage driveItemSearchCollectionPage = graphClient.getSharepoint().getAllDriveItems(listId,siteId);

		graphClient.getSharepoint().getAllItemDriveCollectionPage(driveItemList,driveItemSearchCollectionPage,StringUtils.EMPTY);

		// true = folder / false = files
		Map<Boolean, List<DriveItem>> folderOrFiles = driveItemList.parallelStream().collect(Collectors.partitioningBy(driveItem -> driveItem.folder != null));

		for (DriveItem driveItem : folderOrFiles.get(false)) {
			assert driveItem.parentReference != null;
			getAllFiles(listId, driveItem, driveItem.parentReference.name , siteId);
		}

		for (DriveItem driveItem : folderOrFiles.get(true)) {
			getAllFiles(listId, driveItem, driveItem.name , siteId);
		}
	}

	@Test
	void getSiteId() throws ExecutionException, InterruptedException {
		String siteId = graphClient.getSharepoint().getSiteId(site);
		log.info(siteId);
		assertEquals("docshifter.sharepoint.com,8eb0793b-bd2c-4c55-9f89-c50344d812a6,074d4c88-f1e2-4815-9664-f250b3660685",
				siteId,
				"Site Id should match expected");
	}

	@Test
	@Disabled("Per Juan: it's taking longer and longer to run this test, and it's not proving anything useful!")
	void badCredentialsTest() {

		GraphClient badGraphClient = new GraphClient(MSGraphAuthenticationBuilder.createGraphClient("b081-42d7-a8e1-4c93452d9a3c",
				"3O1p8_T2XatR-PCRd18ywH~DU_tEx.m433", "a545304d-99b4-4706-8c12-f626a2d2a3cb"));

		String siteId = null;

		try {
			siteId = badGraphClient.getSharepoint().getSiteId(site);
		} catch (ClientException e) {
			log.error("ohh noo" + e.getMessage());
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		assertNull(siteId, "Site Id should be NULL");
	}

	@Test
	void processesSpecificFolder() throws ExecutionException, InterruptedException {

		String folderName = "Input/2ndLevel";
		String listName = "Test Input";

		String siteId = graphClient.getSharepoint().getSiteId(site);

		String driveListId = graphClient.getSharepoint().getDriveListIdByListName(siteId,listName);

		String listId = graphClient.getSharepoint().getListIdByDriveId(siteId,driveListId);

		DriveItem folder = graphClient.getSharepoint()
				.getDriveItemByPath(listId, siteId,folderName);

		// Getting Item by parent drive id
		DriveItemCollectionPage itemCollection = graphClient.getSharepoint().listAllItemsFromListById(listId,siteId,folder.id);

		List<DriveItem> lsItems = new ArrayList<>();

		graphClient.getSharepoint().getAllItemDriveCollectionPage(lsItems,itemCollection,StringUtils.EMPTY);

		// true = folder / false = files
		Map<Boolean, List<DriveItem>> folderOrFiles = lsItems.parallelStream().collect(Collectors.partitioningBy(driveItem -> driveItem.folder != null));

		// Process files from main given folder
		for (DriveItem driveItemFolder : folderOrFiles.get(false)) {
			getAllFiles(listId, driveItemFolder, folderName , siteId);
		}

		// Process folders from main given folder
		for (DriveItem driveItemFolder : folderOrFiles.get(true)) {
			getAllFiles(listId, driveItemFolder, folderName , siteId);
		}
	}

	public void getAllFiles(String listId, DriveItem driveItem , String folderName , String siteId) throws ExecutionException, InterruptedException {

		log.info("Filename {} " , driveItem.name);

			if (driveItem.file != null) {

				ListItem listItem = driveItem.listItem;
				assert listItem != null;
				FieldValueSet itemFieldValueSet = listItem.fields;
				assert itemFieldValueSet != null;
				AdditionalDataManager itemFields = itemFieldValueSet.additionalDataManager();
				boolean processedByDS =	itemFields.get("ProcessedByDS").getAsBoolean();

				updateFields(listId, driveItem.listItem.id, graphClient,false,siteId);

				if (!processedByDS) {

					log.info("DOWNLOADING FILE {}", driveItem.name);

					assert driveItem.parentReference != null;
					InputStream fileInputStream = graphClient.getSharepoint().downloadFile(driveItem.parentReference.driveId,driveItem.id,siteId);

					log.info("Downloading file for driveItem.name: {}", driveItem.name);
					downloadFile(fileInputStream, driveItem.name);

					log.info("Updating ProcessedByDS to true for listId: {} and driveItem.listItem.id: {}", listId, driveItem.listItem.id);
					updateFields(listId, driveItem.listItem.id, graphClient,true, siteId);

					log.info("Updating ProcessedByDS to false for listId: {} and driveItem.listItem.id: {}", listId, driveItem.listItem.id);
					updateFields(listId, driveItem.listItem.id, graphClient,false, siteId);
				}
			}

			else {
				assert driveItem.folder != null;
				assert driveItem.folder.childCount != null;
				if (driveItem.folder.childCount > 0) {

					String childFolderName = folderName;

					if(!StringUtils.equals(folderName,driveItem.name)){
						childFolderName = Paths.get(folderName,driveItem.name).toString();
					}

					DriveItem folder = graphClient.getSharepoint()
							.getDriveItemByPath(listId, siteId,childFolderName);


					// Getting Item by parent drive id
					DriveItemCollectionPage itemCollection = graphClient.getSharepoint().listAllItemsFromListById(listId,siteId,folder.id);

					List<DriveItem> lsItems = new ArrayList<>();

					graphClient.getSharepoint().getAllItemDriveCollectionPage(lsItems,itemCollection,StringUtils.EMPTY);

					for (DriveItem driveItem1 : lsItems) {
						getAllFiles(listId, driveItem1, childFolderName , siteId );
					}

				}
			}
		}


	public void updateFields(String listId, String itemId, GraphClient graphClient, boolean processed, String siteId) throws ExecutionException, InterruptedException {
		FieldValueSet fieldValueSet = new FieldValueSet();
		fieldValueSet.additionalDataManager().put("ProcessedByDS", new JsonPrimitive(processed));

		graphClient.getSharepoint().updateFields(listId, itemId, fieldValueSet, siteId);

	}

	public void downloadFile(InputStream in, String fileName) {

		File folder = new File("./target/test-classes/" + UUID.randomUUID() + "/work");
		if (!folder.mkdirs()) {
			log.warn("Hmmm... we did not successfully do the mkdirs call on folder: {}", folder);
		}
		File file = new File(Paths.get(folder.getAbsolutePath(), fileName).toString());

		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			IOUtils.copy(in, outputStream);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Test
	void getCurrentPathTest() {
		String webUrl = "https://docshifterdev.sharepoint.com/Shared%20Documents/Output/watermark/26_Nonclinical_Summary.pdf";

		log.info("Actual url..." + webUrl);
		webUrl = getFolderPath(webUrl);
		log.info("Folder Path..." + webUrl);

		assertNotEquals(StringUtils.EMPTY, webUrl);
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

}
