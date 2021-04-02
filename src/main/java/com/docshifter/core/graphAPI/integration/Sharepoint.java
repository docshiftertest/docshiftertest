package com.docshifter.core.graphAPI.integration;

import com.microsoft.graph.models.ColumnDefinition;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.FieldValueSet;
import com.microsoft.graph.requests.ColumnDefinitionCollectionPage;
import com.microsoft.graph.requests.ColumnDefinitionCollectionRequestBuilder;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.DriveItemCollectionRequestBuilder;
import com.microsoft.graph.requests.GraphServiceClient;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Class responsible for make the request to graph site(Sharepoint).
 * @author Juan Marques created on 06/10/2020
 *
 */
public class Sharepoint {
	
	public static final String DEFAULT_SITE = "root";
	private final GraphServiceClient<?> graphClient;

	public Sharepoint(GraphServiceClient<?> graphClient) {
		this.graphClient = graphClient;
	}

    /**
     * Get drive id of given list.
     * @param siteId the sharepoint site id
     * @param listName The name of the list.
     * @return drive id of given list
     */
    public String getDriveListIdByListName(String siteId , String listName) throws ExecutionException, InterruptedException {
        return this.graphClient
                .sites(siteId)
                .lists(listName)
                .drive()
                .buildRequest()
                .select("id")
                .getAsync()
                .get()
                .id;
    }

    /**
     * Get drive id of given list.
     * @param siteId the sharepoint site id
     * @param driveId The drive id of the list.
     * @return drive id of given list
     */
    public String getListIdByDriveId(String siteId, String driveId) throws ExecutionException, InterruptedException {
        return this.graphClient
                .sites(siteId)
                .drives(driveId)
                .list().buildRequest()
                .select("id")
                .getAsync()
                .get()
                .id;
    }

    /**
     *
     * Find the site id by given site name.
     *
     * @param siteName the sharepoint site name
     * @return the site id
     */
    public String getSiteId(String siteName) throws ExecutionException, InterruptedException {
    	
        if (!StringUtils.isBlank(siteName) && !siteName.equalsIgnoreCase(DEFAULT_SITE)) {

            String site = Paths.get(DEFAULT_SITE, ":/sites", siteName).toString();

            return this.graphClient.sites(site)
                    .buildRequest().getAsync().get().id;
        }
        else {
            return this.graphClient.sites(DEFAULT_SITE).buildRequest().getAsync().get().id;
        }
    }

    /**
     * Retrieve a drive item collection from an given site and library.
     *
     * @param listID the list/library list id.
     * @param siteId the sharepoint site id.
     * @return drive item collection
     */
    public DriveItemCollectionPage getAllDriveItems(String listID, String siteId) throws ExecutionException, InterruptedException {
    	 
        return this.graphClient
                .sites(siteId)
                .lists(listID)
                .drive().root()
                .children()
                .buildRequest()
                .expand("listItem")
                .getAsync().get();
    }

    /**
     * Get any folder or file by given path.
     * @param listId the list/library list id.
     * @param siteId the sharepoint site id.
     * @param path The item path E.G input/2ndLevel
     * @return The folder or file from sharepoint.
     */
    public DriveItem getDriveItemByPath(String listId, String siteId, String path) throws ExecutionException, InterruptedException {
        return this.graphClient
                .sites(siteId)
                .lists(listId)
                .drive()
                .root()
                .itemWithPath(path)
                .buildRequest()
                .getAsync().get();
    }

    /**
     * List all files/folders from given list id.
     * @param listId the list/library list id.
     * @param siteId the sharepoint site id.
     * @param driveId The onedrive id of given files.
     * @return Collection of files / folders.
     */
    public DriveItemCollectionPage listAllItemsFromListById(String listId, String siteId,String driveId) throws ExecutionException, InterruptedException {
        return this.graphClient
                .sites(siteId)
                .lists(listId)
                .drive()
                .items(driveId)
                .children()
                .buildRequest()
                .expand("listItem")
                .getAsync().get();
    }

    /**
     * Update an given field
     *
     * @param listId        the list/library list id.
     * @param itemId        the sharepoint item that you want to update.
     * @param fieldValueSet the actual field with the new value.
     * @param siteId        the sharepoint site id.
     */
    public void updateFields(String listId, String itemId, FieldValueSet fieldValueSet, String siteId) throws ExecutionException, InterruptedException {

        this.graphClient
                .sites(siteId)
                .lists(listId)
                .items(itemId)
                .fields()
                .buildRequest()
                .patchAsync(fieldValueSet).get();
    }

    /**
     * Get all columns from given siteId and ListId
     * @param siteId      the sharepoint site id
     * @param listId      the sharepoint list id
     * @return List of of columns definition
     */
    public List<ColumnDefinition> getColumnDefinition(String siteId, String listId) throws ExecutionException, InterruptedException {

        List<ColumnDefinition> columnDefinitionList = new ArrayList<>();

        ColumnDefinitionCollectionPage collectionPage =  graphClient
                .sites(siteId)
                .lists(listId)
                .columns().buildRequest().getAsync().get();

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
                                                ColumnDefinitionCollectionPage columnDefinitionCollectionPage) throws ExecutionException, InterruptedException {

        columnDefinitionList.addAll(columnDefinitionCollectionPage.getCurrentPage());

        ColumnDefinitionCollectionRequestBuilder nextPage = columnDefinitionCollectionPage.getNextPage();
        if (nextPage != null) {
            getAllPagesFromColumnCollection(columnDefinitionList, nextPage.buildRequest().getAsync().get());
        }
    }

    /**
     * Iterate all lstICollectionPage pages and add to list.
     *
     * @param allItems            the list to aggregate the DriveItems
     * @param driveItemCollectionPage page collection from graph query.
     * @param folderName          only add the folder that you want or leave blank
     *                            to get all
     */
    public void getAllItemDriveCollectionPage(List <DriveItem> allItems, DriveItemCollectionPage driveItemCollectionPage,
                                              String folderName) throws ExecutionException, InterruptedException {

        for (DriveItem item : driveItemCollectionPage.getCurrentPage()) {

            if (item.name.equalsIgnoreCase(folderName)) {
                allItems.add(item);
            } else if (StringUtils.isBlank(folderName)) {
                allItems.add(item);
            }
        }
        DriveItemCollectionRequestBuilder nextPage = driveItemCollectionPage.getNextPage();
        if (nextPage != null) {
            getAllItemDriveCollectionPage(allItems, nextPage.buildRequest().getAsync().get(), folderName);
        }
    }

    /**
     *
     * @param driveId The driver where the file is located.
     * @param itemDriveId the id of the file inside the drive.
     * @param siteId the sharepoint site id
     * @return the file InputStream
     */
    public InputStream downloadFile(String driveId, String itemDriveId , String siteId) throws ExecutionException, InterruptedException {
        return this.graphClient
                .sites(siteId)
                .drives(driveId)
                .items(itemDriveId)
                .content()
                .buildRequest()
                .getAsync()
                .get();
    }
}
