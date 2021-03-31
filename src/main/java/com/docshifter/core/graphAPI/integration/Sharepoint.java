package com.docshifter.core.graphAPI.integration;

import com.microsoft.graph.models.ColumnDefinition;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemSearchParameterSet;
import com.microsoft.graph.models.FieldValueSet;
import com.microsoft.graph.models.ListItem;
import com.microsoft.graph.models.Site;
import com.microsoft.graph.requests.ColumnDefinitionCollectionPage;
import com.microsoft.graph.requests.ColumnDefinitionCollectionRequestBuilder;
import com.microsoft.graph.requests.DriveCollectionPage;
import com.microsoft.graph.requests.DriveCollectionRequestBuilder;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.DriveItemCollectionRequestBuilder;
import com.microsoft.graph.requests.DriveItemSearchCollectionPage;
import com.microsoft.graph.requests.DriveItemSearchCollectionRequestBuilder;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.ListCollectionPage;
import com.microsoft.graph.requests.ListCollectionRequestBuilder;
import com.microsoft.graph.requests.ListItemCollectionPage;
import com.microsoft.graph.requests.ListItemCollectionRequestBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.util.UriUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class responsible for make the request to graph site(Sharepoint).
 * @author Juan Marques created on 06/10/2020
 *
 */
@Log4j2
public class Sharepoint {
	
	public static final String DEFAULT_SITE = "root";
	private final GraphServiceClient<?> graphClient;

	public Sharepoint(GraphServiceClient<?> graphClient) {
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
    public ListCollectionPage getLibrary(String siteId) {

        log.debug("Getting library from {} " , siteId);

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

        log.debug("Getting site id from {} ",siteName);

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
    public DriveItemCollectionPage getAllDriveItems(String listID, String siteId) {
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
	 * Retrieve all drive item from an given site and library.
	 * 
	 * @apiNote We search using StringUtils.EMPTY to get all items
	 * @param listID the list/library list id.
	 * @param siteId the sharepoint site id.
	 * @return drive item search collection
	 */
	public DriveItemSearchCollectionPage searchAllDriveItems(String listID, String siteId) {
		return this.graphClient
				.sites(siteId)
				.lists(listID)
				.drive()
				.root()
                .search(DriveItemSearchParameterSet
                        .newBuilder()
                        .withQ(listID).build())
				.buildRequest()
				.get();
	}

    /**
     * Update an given field
     *
     * @param listId        the list/library list id.
     * @param itemId        the sharepoint item that you want to update.
     * @param fieldValueSet the actual field with the new value.
     * @param siteId        the sharepoint site id.
     */
    public void updateFields(String listId, String itemId, FieldValueSet fieldValueSet, String siteId) {

        this.graphClient
                .sites(siteId)
                .lists(listId)
                .items(itemId)
                .fields()
                .buildRequest()
                .patch(fieldValueSet);
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
    public void getAllItemCollectionPages(List <ListItem> allItems, ListItemCollectionPage lstICollectionPage,
                                          String contentType) {

        for (ListItem item : lstICollectionPage.getCurrentPage()) {

            if (item.contentType.name.equalsIgnoreCase(contentType)) {
                allItems.add(item);
            } else if (StringUtils.isBlank(contentType)) {
                allItems.add(item);
            }
        }

        ListItemCollectionRequestBuilder nextPage = lstICollectionPage.getNextPage();
        if (nextPage != null) {
            getAllItemCollectionPages(allItems, nextPage.buildRequest().get(), contentType);
        }
    }

    /**
     * Get all columns from given siteId and ListId
     * @param siteId      the sharepoint site id
     * @param listId      the sharepoint list id
     * @return List of of columns definition
     */
    public List<ColumnDefinition> getColumnDefinition(String siteId, String listId) {

        List<ColumnDefinition> columnDefinitionList = new ArrayList<>();

        ColumnDefinitionCollectionPage collectionPage =  graphClient
                .sites(siteId)
                .lists(listId)
                .columns().buildRequest().get();

        getAllPagesFromColumnCollection(columnDefinitionList,collectionPage);

        return columnDefinitionList;
    }

    /**
     * Iterate all ColumnDefinition Collection pages and add to list.
     *
     * @param columnDefinitionList          the list to aggregate the DriveItems
     * @param columnDefinitionCollectionPage the given page collection from the request.
     */
    public void getAllPagesFromColumnCollection(List<ColumnDefinition> columnDefinitionList,
                                                ColumnDefinitionCollectionPage columnDefinitionCollectionPage) {

        columnDefinitionList.addAll(columnDefinitionCollectionPage.getCurrentPage());

        ColumnDefinitionCollectionRequestBuilder nextPage = columnDefinitionCollectionPage.getNextPage();
        if (nextPage != null) {
            getAllPagesFromColumnCollection(columnDefinitionList, nextPage.buildRequest().get());
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
    public void getAllItemDriveCollectionPage(List <DriveItem> allItems, DriveItemCollectionPage iBaseCollectionPage,
                                              String folderName) {

        for (DriveItem item : iBaseCollectionPage.getCurrentPage()) {

            if (item.name.equalsIgnoreCase(folderName)) {
                allItems.add(item);
            } else if (StringUtils.isBlank(folderName)) {
                allItems.add(item);
            }
        }
        DriveItemCollectionRequestBuilder nextPage = iBaseCollectionPage.getNextPage();
        if (nextPage != null) {
            getAllItemDriveCollectionPage(allItems, nextPage.buildRequest().get(), folderName);
        }
    }
    
    /**
     * We search in all search collections for the right drive item from given folder
    * @param allItems            the list to aggregate the DriveItems
     * @param itemSearchCollectionPage page search collection from graph query.
     * @param folderName          only add the folder that you want or leave blank
     *                            to get all
     */
    public void getAllPagesFromSearchCollection(List <DriveItem> allItems, DriveItemSearchCollectionPage itemSearchCollectionPage,
                                              String folderName) {
    	
		for (DriveItem item : itemSearchCollectionPage.getCurrentPage()) {
			
			if (item.name.equalsIgnoreCase(folderName)) {
				allItems.add(item);
			} else if (StringUtils.isBlank(folderName)) {
				allItems.add(item);
				
			  //If the folder name contains "/" then we encode the given folder and compare against webURL.
			} else if (folderName.contains("/")) {
				if (item.webUrl.contains(encodeFolderPath(folderName))) {

						log.debug("Item name: {} ", item.name);
						log.debug("Item webUrl: {} ", item.webUrl);

					allItems.add(item);
				}
			}
		}
    	DriveItemSearchCollectionRequestBuilder nextPage = itemSearchCollectionPage.getNextPage();
        if (nextPage != null) {
        	getAllPagesFromSearchCollection(allItems, nextPage.buildRequest().get(), folderName);
        }
    }
    
    /**
     * We encode the given folder from workflow to compare against webUrl.
     * @param folderPath the folder path from workflow E.G. eCTD Sample Content/Module 2/27 Clinical Summary
     * @return the encoded folder path E.G eCTD%20Sample%20Content/Module%202/27%20Clinical%20Summary
     */
    private String encodeFolderPath(String folderPath) {
        return UriUtils.encodePath(folderPath, "UTF-8");
    }

    /**
     * Iterate all IListCollectionPage pages and add to list.
     *
     * @param allItems          the list to aggregate the DriveItems
     * @param lstCollectionPage the given page collection from the request.
     */
    public void getAllListRequestpages(List <com.microsoft.graph.models.List> allItems,
                                       ListCollectionPage lstCollectionPage) {

        allItems.addAll(lstCollectionPage.getCurrentPage());

        ListCollectionRequestBuilder nextPage = lstCollectionPage.getNextPage();
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
    public ListItemCollectionPage getAllContentsInfoFromLibrary(String libraryId, String siteId) {
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
    public DriveItemCollectionPage getAllContentFromSpecificFolder(String listId, String folderId, String siteId) {
        return this.graphClient
                .sites(siteId)
                .lists(listId)
                .drive()
                .items(folderId)
                .children()
                .buildRequest()
                .expand("listItem").get();
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
        ArrayList <com.microsoft.graph.models.List> lists = new ArrayList <>();

        //Retrieving all lists from given site.
        this.getAllListRequestpages(lists, this.getLibrary(siteId));

        //Getting the listName from given list
        Optional <String> listName = lists.stream()
                .filter(library -> library.id.equalsIgnoreCase(list))
                .map(m -> m.name)
                .reduce((a, b) -> a + b);

        //Retrieving the right drive id by given list name
        DriveCollectionPage driveCollection = this.graphClient
                .sites(siteId)
                .drives()
                .buildRequest().get();

        //If listName is not present will thrown an error in the api
        if (listName.isPresent()) {
            driveId = retrieveDriveCollectionId(driveCollection, listName.get());
            
            // This might happens when you have a list that has a different internal name from what shows in the browser
            if(StringUtils.isBlank(driveId)) {
            	log.error("Couldn't retrieve drive id from list {} please check the given list name" , listName.get());
            	return null;
            }
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
    public String retrieveDriveCollectionId(DriveCollectionPage driveCollectionPage, String listName) {

        for (Drive item : driveCollectionPage.getCurrentPage()) {

            if (item.name.equalsIgnoreCase(listName)) {

                return item.id;
            }

            DriveCollectionRequestBuilder nextPage = driveCollectionPage.getNextPage();
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
}
