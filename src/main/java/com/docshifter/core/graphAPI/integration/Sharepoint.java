package com.docshifter.core.graphAPI.integration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.Drive;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.DriveItemUploadableProperties;
import com.microsoft.graph.models.extensions.FieldValueSet;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.ItemReference;
import com.microsoft.graph.models.extensions.ListItem;
import com.microsoft.graph.models.extensions.Site;
import com.microsoft.graph.models.extensions.UploadSession;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.IDriveCollectionPage;
import com.microsoft.graph.requests.extensions.IDriveCollectionRequestBuilder;
import com.microsoft.graph.requests.extensions.IDriveItemCollectionPage;
import com.microsoft.graph.requests.extensions.IDriveItemCollectionRequestBuilder;
import com.microsoft.graph.requests.extensions.IListCollectionPage;
import com.microsoft.graph.requests.extensions.IListCollectionRequestBuilder;
import com.microsoft.graph.requests.extensions.IListItemCollectionPage;
import com.microsoft.graph.requests.extensions.IListItemCollectionRequestBuilder;

/**
 * Class responsible for make the request to graph site(Sharepoint).
 * @author Juan Marques created on 06/10/2020
 *
 */
public class Sharepoint {
	
	private final static String DEFAULT_SITE = "root";
	private final Logger log = Logger.getLogger(Sharepoint.class);
	private final IGraphServiceClient graphClient;

	/**
	 * @param graphClient
	 */
	public Sharepoint(IGraphServiceClient graphClient) {
		this.graphClient = graphClient;
	}
	
	  /**
     * Retrieve List collection from given site.
     *
     * @param siteId the sharepoint site id
     * @return IListCollectionPage all libraries from the given site or root
     * @apiNote Graph API on SharePoint list do not support filtering or ordering
     * results.
     */
    public IListCollectionPage getLibrary(String siteId) {
    	
    	if(log.isDebugEnabled()) {
        	log.debug("Getting library from :" + siteId);
    	}

        return this.graphClient
                .sites(siteId)
                .lists()
                .buildRequest()
                .select("name,id")
                .get();
    }

    /**
     * Find the site id by given site name.
     *
     * @param siteName the sharepoint site name
     * @return the site id
     */
    public String retrieveSiteId(String siteName) {
    	
    	if(log.isDebugEnabled()) {
        	log.debug("Getting site id from :" + siteName);
    	}

        if (!StringUtils.isBlank(siteName) && !siteName.equalsIgnoreCase(DEFAULT_SITE)) {


            String siteID = StringUtils.EMPTY;

            List <Site> sites = this.graphClient.sites().buildRequest().get().getCurrentPage();

            for (Site site : sites) {
                if (site.displayName != null && site.displayName.equalsIgnoreCase(siteName)) {
                    siteID = site.sharepointIds.siteId;
                }
            }

            return siteID;
        }

        return DEFAULT_SITE;
    }

    /**
     * Retrieve a drive item collection from an given site and library.
     *
     * @param listID the list/library list id.
     * @param siteId the sharepoint site id.
     * @return drive item collection
     */
    public IDriveItemCollectionPage getAllDriveItems(String listID, String siteId) {
        return this.graphClient
                .sites(siteId)
                .lists(listID)
                .drive().root()
                .children()
                .buildRequest()
                .select("id,name")
                .get();
    }

    /**
     * Update an given field
     *
     * @param listId        the list/library list id.
     * @param itemId        the sharepoint item that you want to update.
     * @param fieldValueSet the actual field with the new value.
     * @param siteId        the sharepoint site id.
     * @return the updated FieldValueSet
     */
    public FieldValueSet updateFields(String listId, String itemId, FieldValueSet fieldValueSet, String siteId) {

        return this.graphClient
                .sites(siteId)
                .lists(listId)
                .items(itemId)
                .fields()
                .buildRequest()
                .patch(fieldValueSet);
    }

    /**
     * Creates a upload session to upload large files to sharepoint
     *
     * @param listId     the list/library list id.
     * @param fileStream the file converted to stream.
     * @param streamSize the file stream size
     * @param itemPath   the given webUrl that we used to recreate the folder structure.
     * @param siteId     the sharepoint site id
     * @throws IOException if we got an kind of problems while uploading the file to sharepoint
     */
    public void uploadFile(String listId, InputStream fileStream, long streamSize, String itemPath, String siteId) throws IOException {

        //Creates an upload session
        UploadSession uploadSession = graphClient
                .sites(siteId)
                .lists(listId)
                .drive().root()
                .itemWithPath(itemPath)
                .createUploadSession(new DriveItemUploadableProperties())
                .buildRequest().post();

        //Defines the chuck to upload
        ChunkedUploadProvider <DriveItem> chunkedUploadProvider = new ChunkedUploadProvider <>(uploadSession,
                graphClient, fileStream, streamSize, DriveItem.class);

        // Config parameter is an array of integers
        // customConfig[0] indicates the max slice size
        // Max slice size must be a multiple of 320 KiB
        int[] customConfig = {320 * 1024};

        // Do the upload
        chunkedUploadProvider.upload(new IProgressCallback <DriveItem>() {

            @Override
            public void success(DriveItem result) {
                log.info("File " + result.name + " successfully uploaded ");
            }

            @Override
            public void failure(ClientException ex) {
                log.error("Failed to upload the file to " + itemPath, ex);
            }

            @Override
            public void progress(long current, long max) {
                log.trace("Currently loaded :" + current + " of " + max);
            }
        }, customConfig);
    }

    /**
     * Copy folder structure.
     *
     * @param libraryId       The id of the library that you want to explore
     * @param itemId          @param itemId the sharepoint item that you want to update.
     * @param name            the folder name
     * @param parentReference the parent of given folder
     * @param siteId          the sharepoint site id
     */
    public void copyStructure(String libraryId, String itemId, String name, ItemReference parentReference, String siteId) {

        this.graphClient.sites(siteId)
                .lists(libraryId)
                .drive()
                .items(itemId)
                .copy(name, parentReference)
                .buildRequest()
                .post();
    }

    /**
     * Retrieve all items from given library/list.
     *
     * @param libraryId   The id of the library that you want to explore
     * @param lstItems    the list to aggregate the ListItems
     * @param contentType the content type name E.G "Document" or "Folder" or empty
     *                    string to add all contents without filter
     * @param siteId      the sharepoint site id
     */
    public void getLibraryItems(String libraryId, List <ListItem> lstItems, String contentType, String siteId) {
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
    public void getAllItemCollectionPages(List <ListItem> allItems, IListItemCollectionPage lstICollectionPage,
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
    public void getAllItemDriveCollectionPage(List <DriveItem> allItems, IDriveItemCollectionPage iBaseCollectionPage,
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

    /**
     * Iterate all IListCollectionPage pages and add to list.
     *
     * @param allItems          the list to aggregate the DriveItems
     * @param lstCollectionPage the given page collection from the request.
     */
    public void getAllListRequestpages(List <com.microsoft.graph.models.extensions.List> allItems,
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
     * @param siteId    the sharepoint site id
     * @return Page Collection of the provided library.
     */
    public IListItemCollectionPage getAllContentsInfoFromLibrary(String libraryId, String siteId) {
        return this.graphClient
                .sites(siteId)
                .lists(libraryId)
                .items()
                .buildRequest()
                .expand("fields")
                .get();
    }

    /**
     * Get item list from the library expanding the fields.
     *
     * @param listId   the list/library list id.
     * @param folderId the specific folder id
     * @param siteId   the sharepoint site id
     * @return listItem expanding fields.
     */
    public IDriveItemCollectionPage getAllContentFromSpecificFolder(String listId, String folderId, String siteId) {
        return this.graphClient
                .sites(siteId)
                .lists(listId)
                .drive()
                .items(folderId)
                .children()
                .buildRequest()
                .expand("ListItem").get();
    }

    /**
     * Download the file from sharepoint.
     *
     * @param listId the list/library list id.
     * @param itemId the object id from sharepoint
     * @param siteId the sharepoint site id
     * @return InputStream - the downloaded file
     */
    public InputStream getFile(String listId, String itemId, String siteId) {
        return this.graphClient
                .sites(siteId)
                .lists(listId)
                .items(itemId)
                .driveItem()
                .content()
                .buildRequest()
                .get();
    }

    /**
     * @param itemDriveId the item drive id
     * @param siteId      the sharepoint site id
     * @return InputStream - the downloaded file
     */
    public InputStream getFileByDriveId(String itemDriveId, String siteId, String list) {

        String driveId = StringUtils.EMPTY;
        ArrayList <com.microsoft.graph.models.extensions.List> lists = new ArrayList <>();

        //Retrieving all lists from given site.
        this.getAllListRequestpages(lists, this.getLibrary(siteId));

        //Getting the listName from given list
        Optional <String> listName = lists.stream()
                .filter(library -> library.id.equalsIgnoreCase(list))
                .map(m -> m.name)
                .reduce((a, b) -> a + b);

        //Retrieving the right drive id by given list name
        IDriveCollectionPage driveCollection = this.graphClient
                .sites(siteId)
                .drives()
                .buildRequest().get();

        //If listName is not present will thrown an error in the api
        if (listName.isPresent()) {
            driveId = retrieveDriveCollectionId(driveCollection, listName.get());
        }

        return this.graphClient.drives(driveId)
                .items(itemDriveId)
                .content()
                .buildRequest()
                .get();
    }

    /**
     * Look for all pages in the request and find the right drive id
     *
     * @param driveCollectionPage the drive collection with all drivers
     * @param listName            the list to search for the drive
     * @return the drive id
     */
    public String retrieveDriveCollectionId(IDriveCollectionPage driveCollectionPage, String listName) {

        for (Drive item : driveCollectionPage.getCurrentPage()) {

            if (item.name.equalsIgnoreCase(listName)) {

                return item.id;
            }

            IDriveCollectionRequestBuilder nextPage = driveCollectionPage.getNextPage();
            if (nextPage != null) {
                retrieveDriveCollectionId(nextPage.buildRequest().get(), listName);
            }

        }
        return StringUtils.EMPTY;
    }

    /**
     * @param listId           the list/library list id.
     * @param parentFolderPath the parent folder path
     * @param driveItem        the object to be created
     * @param siteId           the sharepoint site id
     * @return {@link DriveItem}
     */
    public DriveItem createChildrenFolder(String listId, String parentFolderPath, DriveItem driveItem, String siteId) {
        return this.graphClient
                .sites(siteId)
                .lists(listId)
                .drive()
                .root()
                .itemWithPath(parentFolderPath)
                .children()
                .buildRequest()
                .post(driveItem);
    }

    /**
     * @param listId    the list/library list id.
     * @param driveItem the object to be created
     * @param siteId    the sharepoint site id
     * @return the created item.
     */
    public DriveItem createRootFolder(String listId, DriveItem driveItem, String siteId) {
        return this.graphClient
                .sites(siteId)
                .lists(listId)
                .drive().root()
                .children()
                .buildRequest()
                .post(driveItem);
    }

    /**
     * Used to build queries to send within the build request.
     *
     * @param name  the param that you want to filter
     * @param value the value to be filtered
     * @return a list with the requested QueryOption
     */
    public List <QueryOption> buildQueryOptions(String name, String value) {
        return Collections.unmodifiableList(Collections.singletonList(new QueryOption(name, value)));

    }

	


}
