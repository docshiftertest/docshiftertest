package com.docshifter.core.graphAPI;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.microsoft.graph.logger.LoggerLevel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.DriveItemUploadableProperties;
import com.microsoft.graph.models.extensions.FieldValueSet;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.ItemReference;
import com.microsoft.graph.models.extensions.ListItem;
import com.microsoft.graph.models.extensions.UploadSession;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.IDriveItemCollectionPage;
import com.microsoft.graph.requests.extensions.IDriveItemCollectionRequestBuilder;
import com.microsoft.graph.requests.extensions.IListCollectionPage;
import com.microsoft.graph.requests.extensions.IListCollectionRequestBuilder;
import com.microsoft.graph.requests.extensions.IListItemCollectionPage;
import com.microsoft.graph.requests.extensions.IListItemCollectionRequestBuilder;

/**
 * @author Juan Marques created on 06/08/2020
 *
 *@apiNote To use beta api please set graphClient.setServiceRoot("https://graph.microsoft.com/beta");
 */
public class GraphClient {
	
	private final Logger log = Logger.getLogger(GraphClient.class);

	private final IGraphServiceClient graphClient;
	private final static String DEFAULT_SITE = "root";

	public GraphClient(IGraphServiceClient graphClient) {
		this.graphClient = graphClient;
		//graphClient.setServiceRoot("https://graph.microsoft.com/beta");
		graphClient.getLogger().setLoggingLevel(LoggerLevel.DEBUG);
	}

	/**
	 *
	 *
	 * @param siteId TODO
	 * @return IListCollectionPage all libraries from the root
	 * @apiNote Graph API on SharePoint list do not support filtering or ordering
	 *          results.
	 */
	public IListCollectionPage getLibrary(String siteId) {
		return this.graphClient.sites(siteId).lists().buildRequest().select("name,id").get();
	}
	
	/**
	 * Find the site id by given site name.
	 * @param siteId as String
	 * @return the site id
	 */
	public String retrieveSiteId(String siteId) {

		if (!StringUtils.isBlank(siteId) && !siteId.equalsIgnoreCase(DEFAULT_SITE)) {
			return this.graphClient.sites().buildRequest().get().getCurrentPage().stream()
					.filter(i -> i.displayName.equalsIgnoreCase(siteId)).findFirst().get().sharepointIds.siteId;
		}

		return DEFAULT_SITE;
	}

	public IDriveItemCollectionPage getAllDriveItems(String listID, String siteId) {
		return this.graphClient.sites(siteId).lists(listID).drive().root().children().buildRequest().select("id,name")
				.get();
	}
	
	public FieldValueSet updateFields(String listId, String itemId, FieldValueSet fieldValueSet, String siteId) {

		return this.graphClient.sites(siteId).lists(listId).items(itemId).fields().buildRequest().patch(fieldValueSet);
	}

	/**
	 * 
	 * @param listId
	 * @param fileStream
	 * @param streamSize
	 * @param itemPath
	 * @param siteId TODO
	 * @throws IOException
	 */
	public void uploadFile(String listId, InputStream fileStream, long streamSize, String itemPath, String siteId) throws IOException {

		UploadSession uploadSession = graphClient.sites(siteId).lists(listId).drive().root().itemWithPath(itemPath)
				.createUploadSession(new DriveItemUploadableProperties()).buildRequest().post();

		ChunkedUploadProvider<DriveItem> chunkedUploadProvider = new ChunkedUploadProvider<>(uploadSession,
				graphClient, fileStream, streamSize, DriveItem.class);

		// Config parameter is an array of integers
		// customConfig[0] indicates the max slice size
		// Max slice size must be a multiple of 320 KiB
		int[] customConfig = { 320 * 1024 };

		// Do the upload
		chunkedUploadProvider.upload(new IProgressCallback<DriveItem>() {

			@Override
			public void success(DriveItem result) {
				log.info("File " + result.name + " successfully uploaded ");
			}

			@Override
			public void failure(ClientException ex) {
				log.error("Failed to upload the file to " + itemPath,ex);
			}

			@Override
			public void progress(long current, long max) {

			}
		}, customConfig);
	}

	/**
	 * Copy folder.
	 * 
	 * @param libraryID
	 * @param itemID
	 * @param name
	 * @param parentReference
	 * @param siteId TODO
	 */
	public void copyStructure(String libraryID, String itemID, String name, ItemReference parentReference, String siteId) {

		graphClient.sites(siteId).lists(libraryID).drive().items(itemID).copy(name, parentReference).buildRequest()
				.post();
	}

	/**
	 * 
	 * @param libraryId   libraryId the id of the library that you want to explore
	 * @param lstItems    the list to aggregate the ListItems
	 * @param contentType the content type name E.G "Document" or "Folder" or empty
	 *                    string to add all contents without filter
	 * @param siteId TODO
	 */
	public void getLibraryItems(String libraryId, List<ListItem> lstItems, String contentType, String siteId) {
		getAllItemCollectionPages(lstItems, this.getAllContentsInfoFromLibrary(libraryId, siteId), contentType);
	}

	/**
	 * Iterate all lstICollectionPage pages and add to list.
	 * 
	 * @param allItems           the list to aggregate the ListItems
	 * @param lstICollectionPage page collection from graph query.
	 * @param contentType        the content type name E.G "Document" or "Folder" or
	 *                           empty string to add all contents without filter
	 */
	public void getAllItemCollectionPages(List<ListItem> allItems, IListItemCollectionPage lstICollectionPage,
			String contentType) {

		for (ListItem item : lstICollectionPage.getCurrentPage()) {

			if (item.contentType.name.equalsIgnoreCase(contentType)) {
				allItems.add(item);
			} else if (StringUtils.isBlank(contentType)) {
				allItems.add(item);
			}
		}

		IListItemCollectionRequestBuilder nextPage = lstICollectionPage.getNextPage();
		if (nextPage != null) {
			getAllItemCollectionPages(allItems, nextPage.buildRequest().get(), contentType);
		}
	}

	/**
	 * Iterate all lstICollectionPage pages and add to list.
	 * 
	 * @param allItems            the list to aggregate the DriveItems
	 * @param iBaseCollectionPage page collection from graph query.
	 * @param folderName          only add the folder that you want or leave blank
	 *                            to get all
	 */
	public void getAllItemDriveCollectionPage(List<DriveItem> allItems, IDriveItemCollectionPage iBaseCollectionPage,
			String folderName) {

		for (DriveItem item : iBaseCollectionPage.getCurrentPage()) {

			if (item.name.equalsIgnoreCase(folderName)) {
				allItems.add(item);
			} else if (StringUtils.isBlank(folderName)) {
				allItems.add(item);
			}
		}
		IDriveItemCollectionRequestBuilder nextPage = iBaseCollectionPage.getNextPage();
		if (nextPage != null) {
			getAllItemDriveCollectionPage(allItems, nextPage.buildRequest().get(), folderName);
		}
	}

	public void getAllListRequestpages(List<com.microsoft.graph.models.extensions.List> allItems,
			IListCollectionPage lstCollectionPage) {

		allItems.addAll(lstCollectionPage.getCurrentPage());

		IListCollectionRequestBuilder nextPage = lstCollectionPage.getNextPage();
		if (nextPage != null) {
			getAllListRequestpages(allItems, nextPage.buildRequest().get());
		}
	}

	/**
	 * Get page collection from the library expanding the fields.
	 * 
	 * @param libraryId the id of the library that you want to explore
	 * @param siteId TODO
	 * @return Page Collection of the provided library.
	 */
	public IListItemCollectionPage getAllContentsInfoFromLibrary(String libraryId, String siteId) {
		return this.graphClient.sites(siteId).lists(libraryId).items().buildRequest().expand("fields").get();
	}

	/**
	 * Get item list from the library expanding the fields.
	 * 
	 * @param listId   the library id
	 * @param folderId the specific folder id
	 * @param siteId TODO
	 * @return listItem expanding fields.
	 */
	public IDriveItemCollectionPage getAllContentFromSpecificFolder(String listId, String folderId, String siteId) {
		return this.graphClient.sites(siteId).lists(listId).drive().items(folderId).children().buildRequest()
				.expand("ListItem").get();
	}

	/**
	 * 
	 * @param listId the library id.
	 * @param itemId the object id from sharepoint
	 * @param siteId TODO
	 * @return InputStream - the downloaded file
	 */
	public InputStream getFile(String listId, String itemId, String siteId) {
		return this.graphClient.sites(siteId).lists(listId).items(itemId).driveItem().content().buildRequest().get();
	}

	/**
	 * 
	 * @param itemDriveId the item drive id
	 * @param siteId TODO
	 * @return InputStream - the downloaded file
	 */
	public InputStream getFileByDriveId(String itemDriveId, String siteId,String listID) {
		return this.graphClient.drive().list().drive().items(itemDriveId).content().buildRequest().get();
	}

	public InputStream getFileByPath(String path,String siteId){
		return this.graphClient.drive().root().itemWithPath(path).content().buildRequest().get();
				//.itemWithPath(path).content().buildRequest().get();
	}

	/**
	 * @param listId           the libraryID
	 * @param parentFolderPath the parent folder path
	 * @param driveItem        the object to be created
	 * @param siteId TODO
	 * @return {@link DriveItem}
	 */
	public DriveItem createChildrenFolder(String listId, String parentFolderPath, DriveItem driveItem, String siteId) {
		return this.graphClient.sites(siteId).lists(listId).drive().root().itemWithPath(parentFolderPath).children()
				.buildRequest().post(driveItem);
	}

	/**
	 * @param listId    the libraryID
	 * @param driveItem the object to be created
	 * @param siteId TODO
	 * @return {@link DriveItem}
	 */
	public DriveItem createRootFolder(String listId, DriveItem driveItem, String siteId) {
		return this.graphClient.sites(siteId).lists(listId).drive().root().children().buildRequest().post(driveItem);
	}

	public List<QueryOption> buildQueryOptions(String name, String value) {
		return Collections.unmodifiableList(Collections.singletonList(new QueryOption(name, value)));

	}

}
