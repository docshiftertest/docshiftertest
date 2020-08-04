package com.docshifter.core.sharepointConnection;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.docshifter.core.http.error.handler.HttpResponseErrorHandler;

/**
 * @author Juan Marques created on 29/07/2020
 */
public class SharePointClient {

	private static final Logger log = LoggerFactory.getLogger(SharePointClient.class);
	private final RestTemplate restTemplate;
	private final AuthTokenHelper tokenHelper;
	private MultiValueMap<String, String> headers;
	private HeadersHelper headerHelper;

	/**
	 * @param user      The user email to access sharepoint online site.
	 * @param passwd    the user password to access sharepoint online site.
	 * @param domain    the domain without protocol and no uri like
	 *                  docshifterdev.sharepoint.com
	 * @param spSiteUrl The sharepoint site URI like /sites/docshiftersosite
	 */
	private SharePointClient(String user, String passwd, String domain, String spSiteUrl) {
		this.restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new HttpResponseErrorHandler());
		String spSiteUrl1 = spSiteUrl;
		if (spSiteUrl1.endsWith("/")) {
			log.debug("spSiteUri ends with /, removing character");
			spSiteUrl1 = spSiteUrl1.substring(0, spSiteUrl1.length() - 1);
		}
		if (!spSiteUrl1.startsWith("/")) {
			log.debug("spSiteUri doesnt start with /, adding character");
			spSiteUrl1 = String.format("%s%s", "/", spSiteUrl1);
		}
		this.tokenHelper = new AuthTokenHelper(this.restTemplate, user, passwd, domain, spSiteUrl);
		try {
			log.debug("Auth initialization performed successfully. Now you can perform actions on the site.");
			this.tokenHelper.init();
			this.headerHelper = new HeadersHelper(this.tokenHelper);
		} catch (Exception e) {
			log.error(
					"Initialization failed!! Please check the user, pass, domain and spSiteUri parameters you provided");
			log.debug("Authentication has failed", e);
		}
	}

	public static SharePointClient createSharePointClient(String user, String passwd, String domain, String spSiteUrl) {
		return new SharePointClient(user, passwd, domain, spSiteUrl);
	}

	public void refreshToken() {
		try {
			this.tokenHelper.init();
		} catch (Exception e) {
			log.debug("Token could not be refreshed", e);
		}
	}

	/**
	 * Method to get json string wich you can transform to a JSONObject and get data
	 * from it.
	 *
	 * @param data- Data to be sent as query (look at the rest api documentation on
	 *              how to include search filters).
	 * @return String representing a json object if the auth is correct.
	 */
	public JSONObject getAllLists(String data) throws Exception {
		log.debug("getAllLists {}", data);
		headers = headerHelper.getGetHeaders(false);

		RequestEntity<String> requestEntity = new RequestEntity<>(data, headers, HttpMethod.GET,
				this.tokenHelper.getSharepointSiteUrl("/_api/web/lists"));

		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

		return new JSONObject(responseEntity.getBody());
	}

	/**
	 * @param folder            folder server relative URL to retrieve
	 *                          (/SITEURL/folder)
	 * @param jsonExtendedAttrs extended body for the query.
	 * @return json string representing folder info.
	 */
	public JSONObject getFolderByRelativeUrl(String folder, String jsonExtendedAttrs) throws Exception {
		log.debug("getFolderByRelativeUrl {} jsonExtendedAttrs {}", folder, jsonExtendedAttrs);
		headers = headerHelper.getGetHeaders(false);

		URI url = this.tokenHelper.getSharepointSiteUrl("/_api/web/GetFolderByServerRelativeUrl('" + folder + "')");

		RequestEntity<String> requestEntity = new RequestEntity<>(jsonExtendedAttrs, headers, HttpMethod.GET, url);

		log.info(url.toString());
		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

		return new JSONObject(responseEntity.getBody());
	}

	public Boolean deleteFile(String fileServerRelativeUrl) throws Exception {
		log.debug("Deleting file {} ", fileServerRelativeUrl);

		headers = headerHelper.getDeleteHeaders();

		RequestEntity<String> requestEntity = new RequestEntity<>("{}", headers, HttpMethod.POST, this.tokenHelper
				.getSharepointSiteUrl("/_api/web/GetFileByServerRelativeUrl('" + fileServerRelativeUrl + "')"));

		restTemplate.exchange(requestEntity, String.class);
		return Boolean.TRUE;
	}

	public Resource downloadFile(String fileServerRelativeUrl) throws Exception {
		log.debug("Downloading file {} ", fileServerRelativeUrl);

		headers = headerHelper.getGetHeaders(true);

		RequestEntity<String> requestEntity = new RequestEntity<>("", headers, HttpMethod.GET, this.tokenHelper
				.getSharepointSiteUrl("/_api/web/GetFileByServerRelativeUrl('" + fileServerRelativeUrl + "')/$value"));

		ResponseEntity<Resource> response = restTemplate.exchange(requestEntity, Resource.class);
		return response.getBody();
	}

	public JSONObject uploadFile(String folder, Resource resource, boolean overrideExistingFile, String fileName)
			throws Exception {
		log.debug("Uploading file {} to folder {}", fileName, folder);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "SP.ListItem");

		headers = headerHelper.getPostHeaders("");
		headers.remove("Content-Length");

		byte[] resBytes = IOUtils.readFully(resource.getInputStream(), (int) resource.contentLength());

		RequestEntity<byte[]> requestEntity = new RequestEntity<>(resBytes, headers, HttpMethod.POST,
				this.tokenHelper.getSharepointSiteUrl("/_api/web/GetFolderByServerRelativeUrl('" + folder
						+ "')/Files/add(url='" + fileName + "',overwrite=" + overrideExistingFile + ")"));

		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

		log.debug("Updated file metadata Status {} , response body {}", responseEntity.getStatusCode(),responseEntity.getBody());

		return new JSONObject(responseEntity.getStatusCode());
	}

	public JSONObject uploadFileAndUpdateMetaData(String folder, Resource resource, JSONObject jsonMetadata,
			boolean overrideExistingFile, String fileName) throws Exception {
		log.debug("Uploading file {} to folder {}", fileName, folder);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", "SP.ListItem");
		jsonMetadata.put("__metadata", jsonObject);

		headers = headerHelper.getPostHeaders("");
		headers.remove("Content-Length");

		byte[] resBytes = IOUtils.readFully(resource.getInputStream(), (int) resource.contentLength());

		RequestEntity<byte[]> requestEntity = new RequestEntity<>(resBytes, headers, HttpMethod.POST,
				this.tokenHelper.getSharepointSiteUrl("/_api/web/GetFolderByServerRelativeUrl('" + folder
						+ "')/Files/add(url='" + fileName + "',overwrite=" + overrideExistingFile + ")"));

		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

		String fileInfoStr = responseEntity.getBody();

		log.debug("Retrieved response from server with json");

		JSONObject jsonFileInfo = new JSONObject(fileInfoStr);
		String serverRelFileUrl = jsonFileInfo.getJSONObject("d").getString("ServerRelativeUrl");

		log.debug("File uploaded to URI", serverRelFileUrl);
		String metadata = jsonMetadata.toString();
		headers = headerHelper.getUpdateHeaders(metadata);

		log.debug("Updating file adding metadata {}", jsonMetadata);

		RequestEntity<String> requestEntity1 = new RequestEntity<>(metadata, headers, HttpMethod.POST,
				this.tokenHelper.getSharepointSiteUrl(
						"/_api/web/GetFileByServerRelativeUrl('" + serverRelFileUrl + "')/listitemallfields"));
		ResponseEntity<String> responseEntity1 = restTemplate.exchange(requestEntity1, String.class);
		log.debug("Updated file metadata Status {}", responseEntity1.getStatusCode());

		return new JSONObject(responseEntity1);
	}

	public JSONObject updateFileMetadata(String fileServerRelatUrl, JSONObject jsonMetadata) throws Exception {
		JSONObject meta = new JSONObject();
		meta.put("type", "SP.ListItem");
		jsonMetadata.put("__metadata", meta);
		log.info("File: " + fileServerRelatUrl + " updated ");
		String metadata = jsonMetadata.toString();
		headers = headerHelper.getUpdateHeaders(metadata);
		log.debug("Updating file adding metadata {}", jsonMetadata);

		RequestEntity<String> requestEntity1 = new RequestEntity<>(metadata, headers, HttpMethod.POST,
				this.tokenHelper.getSharepointSiteUrl(
						"/_api/web/GetFileByServerRelativeUrl('" + fileServerRelatUrl + "')/listitemallfields")

		);

		ResponseEntity<String> responseEntity1 = restTemplate.exchange(requestEntity1, String.class);

		log.debug("Updated file metadata Status {}", responseEntity1.getStatusCode());
		return new JSONObject(responseEntity1);
	}

	public JSONObject updateFolderMetadata(String folderServerRelatUrl, JSONObject jsonMetadata) throws Exception {
		JSONObject meta = new JSONObject();
		meta.put("type", "SP.Folder");
		jsonMetadata.put("__metadata", meta);
		log.debug("File uploaded to URI", folderServerRelatUrl);
		String metadata = jsonMetadata.toString();
		headers = headerHelper.getUpdateHeaders(metadata);
		log.debug("Updating file adding metadata {}", jsonMetadata);

		RequestEntity<String> requestEntity1 = new RequestEntity<>(metadata, headers, HttpMethod.POST,
				this.tokenHelper.getSharepointSiteUrl(
						"/_api/web/GetFolderByServerRelativeUrl('" + folderServerRelatUrl + "')/listitemallfields"));
		ResponseEntity<String> responseEntity1 = restTemplate.exchange(requestEntity1, String.class);
		log.debug("Updated file metadata Status {}", responseEntity1.getStatusCode());
		return new JSONObject(responseEntity1);
	}

	public JSONObject createFolder(String baseFolderRemoteRelativeUrl, String folder, JSONObject payload)
			throws Exception {
		log.debug("createFolder baseFolderRemoteRelativeUrl {} folder {}", baseFolderRemoteRelativeUrl, folder);
		if (payload == null) {
			payload = new JSONObject();
		}
		JSONObject meta = new JSONObject();
		meta.put("type", "SP.Folder");
		payload.put("__metadata", meta);
		payload.put("ServerRelativeUrl", baseFolderRemoteRelativeUrl + "/" + folder);
		String payloadStr = payload.toString();
		headers = headerHelper.getPostHeaders(payloadStr);

		RequestEntity<String> requestEntity = new RequestEntity<>(payloadStr, headers, HttpMethod.POST,
				this.tokenHelper.getSharepointSiteUrl(
						"/_api/web/GetFolderByServerRelativeUrl('" + baseFolderRemoteRelativeUrl + "')/folders"));
		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
		return new JSONObject(responseEntity.getStatusCode());
	}

	/**
	 * @param from                 source file path
	 * @param to                   destination file path
	 * @param overrideExistingFile Overwrite = 1; AllowBrokenThickets (move even if
	 *                             supporting files are separated from the file) =
	 *                             8.
	 */
	public JSONObject moveFile(String from, String to, int overrideExistingFile) throws Exception {
		log.debug("movingFile from {} to {}", from, to);
		headers = headerHelper.getPostHeaders("");

		RequestEntity<String> requestEntity = new RequestEntity<>("", headers, HttpMethod.POST,
				this.tokenHelper.getSharepointSiteUrl("/_api/web/GetFileByServerRelativeUrl('"
						+ UriUtils.encodeQuery(from, StandardCharsets.UTF_8) + "')/moveto(newUrl='"
						+ UriUtils.encodeQuery(to, StandardCharsets.UTF_8) + "',flags=" + overrideExistingFile + ")"));
		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
		return new JSONObject(responseEntity);
	}

	public JSONObject deleteFolder(String folderRemoteRelativeUrl) throws Exception {
		log.debug("Deleting folder {}", folderRemoteRelativeUrl);
		headers = headerHelper.getDeleteHeaders();

		RequestEntity<String> requestEntity = new RequestEntity<>("", headers, HttpMethod.POST, this.tokenHelper
				.getSharepointSiteUrl("/_api/web/GetFolderByServerRelativeUrl('" + folderRemoteRelativeUrl + "')"));

		ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

		return new JSONObject(response.getStatusCode());
	}

	public JSONObject getAllFilesFromFolder(String folder) throws Exception {
		log.debug("getAllFilesFromFolder {}", folder);
		headers = headerHelper.getGetHeaders(false);

		URI url = this.tokenHelper.getSharepointSiteUrlWithParam(
				"/_api/web/GetFolderByServerRelativeUrl('" + folder + "')/files", "$expand", "ListItemAllFields");

		RequestEntity<String> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, url);

		log.debug("URI: " + requestEntity.getUrl());

		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

		return new JSONObject(responseEntity.getBody());
	}

	public JSONObject getFilesProperties(String folder, String filePath) throws Exception {
		log.debug("getFolderByRelativeUrl {}", folder);
		headers = headerHelper.getGetHeaders(false);

		RequestEntity<String> requestEntity = new RequestEntity<>(headers, HttpMethod.GET,
				this.tokenHelper.getSharepointSiteUrl(
						"/_api/web/GetFileByServerRelativePath(decodedurl='" + folder + filePath + "')/Properties")

		);
		log.debug("uri: " + this.tokenHelper.getSharepointSiteUrl(
				"/_api/web/GetFileByServerRelativePath(decodedurl='" + folder + filePath + "')/Properties"));

		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

		return new JSONObject(responseEntity.getBody());
	}

	public HeadersHelper getHeaderHelper() {
		return headerHelper;
	}

	public AuthTokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
