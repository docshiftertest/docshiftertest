package com.docshifter.core.utils.veeva;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Requests{
	private static final Logger logger = Logger.getLogger(Requests.class);
	private final static char[] MULTIPART_CHARS =
			"-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
					.toCharArray();
	/**
	 * https://developer.veevavault.com/api/18.3/#submitting-a-query
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse getQuery(String sessionId, String host, String apiVersion, String query) throws Exception {

		String urlStr = "https://" + host + "/api/" + apiVersion + "/query";
		URL url = new URL(urlStr);

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("q", query);
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (postData.length() != 0) {
				postData.append('&');
			}
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setDoOutput(true);

		OutputStreamWriter osw = new OutputStreamWriter(
				con.getOutputStream());
		osw.write(postData.toString());
		osw.flush();
		osw.close();

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'GET' request to URL : " + url);
		logger.debug("GET parameters : " + new String(postDataBytes));
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#bulk-document-state-change => but this method is only changing a single document/binder
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse changeStatus(String sessionId, String host, String type, String apiVersion,
                                             String itemId, String lifecycleAction, String version) throws Exception {

		String urlStr = null;

		if (StringUtils.isBlank(version)) {
			//retrieve the last version of that document/binder
			VeevaResponse veevaResponse = getVersions(sessionId, host, type, apiVersion, itemId);
			if (veevaResponse instanceof VeevaBadResponse)
				return veevaResponse;

			JSONArray listIds = (JSONArray) new JSONObject(new String(veevaResponse.getContentBody())).get("versions");
			urlStr = getUrlWithLatestVersionFromGetVersions(listIds) + "/lifecycle_actions/" + lifecycleAction;


		} else {
			String[] majorMinorVersion = version.split("\\.");
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/" + type + "/" + itemId + "/versions/" + majorMinorVersion[0] + "/" + majorMinorVersion[1] + "/lifecycle_actions/" + lifecycleAction;
		}


		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("PUT");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setDoOutput(true);

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'GET' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-user-actions
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse getUserLifecycleActions(String sessionId, String host, String apiVersion, String type, String itemId, String version) throws Exception {

		String urlStr;
		if (StringUtils.isNotBlank(version)) {
			String[] majorMinorVersion = version.split("\\.");
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/" + type + "/" + itemId + "/versions/" + majorMinorVersion[0] + "/" + majorMinorVersion[1] + "/lifecycle_actions";
		} else {
			//retrieve the last version of that document/binder
			VeevaResponse veevaResponse = getVersions(sessionId, host, type, apiVersion, itemId);
			if (veevaResponse instanceof VeevaBadResponse)
				return veevaResponse;

			JSONArray listIds = (JSONArray) new JSONObject(new String(veevaResponse.getContentBody())).get("versions");
			urlStr = getUrlWithLatestVersionFromGetVersions(listIds) + "/lifecycle_actions/";
		}

		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setDoOutput(true);

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'GET' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-binder
	 * https://developer.veevavault.com/api/18.3/#retrieve-document
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse retrieveMetadata(String sessionId, String host, String type, String apiVersion, String itemId, String version) throws Exception {

		String urlStr;
		if (StringUtils.isNotBlank(version)) {
			String[] majorMinorVersion = version.split("\\.");
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/" + type + "/" + itemId + "/versions/" + majorMinorVersion[0] + "/" + majorMinorVersion[1];
		} else {
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/" + type + "/" + itemId;
		}

		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setDoOutput(true);

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'GET' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#download-document-file
	 * https://developer.veevavault.com/api/18.3/#download-document-version-file
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse getFileContent(String sessionId, String host, String apiVersion, String itemId, String version) throws Exception {

		String urlStr;
		if (StringUtils.isNotBlank(version)) {
			String[] majorMinorVersion = version.split("\\.");
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/documents/" + itemId + "/versions/" + majorMinorVersion[0] + "/" + majorMinorVersion[1] + "/file";
		} else {
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/documents/" + itemId + "/file";
		}

		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setDoOutput(true);

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'GET' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-document-version-renditions
	 * https://developer.veevavault.com/api/18.3/#download-document-version-rendition-file
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse getRenditionContent(String sessionId, String host, String apiVersion, String itemId, String renditionType, String version) throws Exception {

		String urlStr;
		if (StringUtils.isNotBlank(version)) {
			String[] majorMinorVersion = version.split("\\.");
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/documents/" + itemId + "/versions/" + majorMinorVersion[0] + "/" + majorMinorVersion[1] + "/renditions/" + renditionType;
		} else {
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/documents/" + itemId + "/renditions/" + renditionType;
		}

		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setDoOutput(true);

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'GET' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**https://developer.veevavault.com/api/18.3/#user-name-and-password
	 *
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse getSession(String host, String apiVersion, String user, String pass) throws Exception {
		String urlStr = "https://" + host + "/api/" + apiVersion + "/auth";
		URL url = new URL(urlStr);

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("username", user);
		params.put("password", pass);
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (postData.length() != 0) {
				postData.append('&');
			}
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}

		byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setDoOutput(true);

		OutputStreamWriter osw = new OutputStreamWriter(
				con.getOutputStream());
		osw.write(postData.toString());
		osw.flush();
		osw.close();

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'POST' request to URL : " + url);
		logger.debug("Post parameters : " + new String(postDataBytes));
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-document-versions
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse getVersions(String sessionId, String host, String type, String apiVersion, String itemId) throws Exception {

		String urlStr = "https://" + host + "/api/" + apiVersion + "/objects/" + type + "/" + itemId + "/versions";

		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setDoOutput(true);

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'GET' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#create-documents
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse createDocument(String sessionId, String host, String apiVersion, Map<String, Object> values, byte[] byteFiles, String fileName)
			throws Exception {
		String urlStr = "https://" + host + "/api/" + apiVersion + "/objects/documents";
		URL url = new URL(urlStr);
		String crlf = "\r\n";
		String boundary = generateBoundary();

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(false);

		OutputStream directOutput = con.getOutputStream();
		PrintWriter body = new PrintWriter(new OutputStreamWriter(directOutput, StandardCharsets.UTF_8), true);

		body.append(crlf);
		for (Map.Entry<String, Object> param : values.entrySet()) {
			addOpenDelimiter(body,boundary,crlf);
			addSimpleFormData(param.getKey(), param.getValue().toString(), body, crlf);
		}

		addOpenDelimiter(body,boundary,crlf);
		addFileData("file", fileName, byteFiles, body, directOutput, crlf);
		addCloseDelimiter(body, boundary, crlf);

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'POST' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#update-single-document
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse updateSingleDocument(String sessionId, String host, String apiVersion, String itemId, Map<String, Object> values, byte[] byteFiles, String fileName)
			throws Exception {

		String urlStr = "https://" + host + "/api/" + apiVersion + "/objects/documents/"+itemId;
		URL url = new URL(urlStr);
		String crlf = "\r\n";
		String boundary = generateBoundary();

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(false);

		OutputStream directOutput = con.getOutputStream();
		PrintWriter body = new PrintWriter(new OutputStreamWriter(directOutput, StandardCharsets.UTF_8), true);

		body.append(crlf);
		for (Map.Entry<String, Object> param : values.entrySet()) {
			addOpenDelimiter(body,boundary,crlf);
			addSimpleFormData(param.getKey(), param.getValue().toString(), body, crlf);
		}

		addOpenDelimiter(body,boundary,crlf);
		addFileData("file", fileName, byteFiles, body, directOutput, crlf);
		addCloseDelimiter(body, boundary, crlf);

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'POST' request to URL : " + url);
		//logger.debug("Post parameters : " + new String(postDataBytes));
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#create-binder-relationship
	 * https://developer.veevavault.com/api/18.3/#create-single-document-relationship
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse createRelations(String sessionId, String host, String type, String apiVersion, String itemId, String version, String docIdRelation, String relationType)
			throws Exception {

		String urlStr;
		String[] majorMinorVersion;
		if (StringUtils.isNotBlank(version)) { //if we’re given a version we don’t need to //retrieve the last version of that document/binder etc.
			majorMinorVersion = version.split("\\.");
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/" + type + "/" + itemId + "/versions/" + majorMinorVersion[0] + "/" + majorMinorVersion[1] + "/relationships/";
		} else {
			//retrieve the last version of that document/binder
			VeevaResponse veevaResponse = getVersions(sessionId, host, type, apiVersion, itemId);
			if (veevaResponse instanceof VeevaBadResponse)
				return veevaResponse;

			JSONArray listIds = (JSONArray) new JSONObject(new String(veevaResponse.getContentBody())).get("versions");
			urlStr = getUrlWithLatestVersionFromGetVersions(listIds) + "/relationships/";

		}

		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setDoOutput(true);

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("target_doc_id__v", docIdRelation);
		params.put("relationship_type__v", relationType);
		//params.put("target_major_version__v", majorMinorVersion[0]);
		//params.put("target_minor_version__v", majorMinorVersion[1]);

		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (postData.length() != 0) {
				postData.append('&');
			}
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}

		OutputStreamWriter osw = new OutputStreamWriter(
				con.getOutputStream());
		osw.write(postData.toString());
		osw.flush();
		osw.close();

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'POST' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#retrieve-document-relationship
	 * https://developer.veevavault.com/api/18.3/#retrieve-binder-relationship
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse retrieveAllRelations(String sessionId, String host, String type, String apiVersion, String itemId, String version)
			throws Exception {

		String urlStr;
		String[] majorMinorVersion;
		if (StringUtils.isNotBlank(version)) {
			majorMinorVersion = version.split("\\.");
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/" + type + "/" + itemId + "/versions/" + majorMinorVersion[0] + "/" + majorMinorVersion[1] + "/relationships";
		} else {
			//retrieve the last version of that document/binder
			VeevaResponse veevaResponse = getVersions(sessionId, host, type, apiVersion, itemId);
			if (veevaResponse instanceof VeevaBadResponse)
				return veevaResponse;

			JSONArray listIds = (JSONArray) new JSONObject(new String(veevaResponse.getContentBody())).get("versions");
			urlStr = getUrlWithLatestVersionFromGetVersions(listIds) + "/relationships/";

		}

		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setDoOutput(true);

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'GET' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#add-document-to-binder
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse addDocumentToBinder(String sessionId, String host, String apiVersion, String binderId, String orderV, String docId, String parentId)
			throws Exception {

		String urlStr = "https://" + host + "/api/" + apiVersion + "/objects/binders/" + binderId + "/documents";

		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setDoOutput(true);

		Map<String, Object> params = new LinkedHashMap<>();
		params.put("document_id__v", docId);
		if (parentId != null)
			params.put("parent_id__v", parentId);
		else
			params.put("parent_id__v", "");
		if (orderV != null)
			params.put("order__v", orderV); //optional

		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (postData.length() != 0) {
				postData.append('&');
			}
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}

		OutputStreamWriter osw = new OutputStreamWriter(
				con.getOutputStream());
		osw.write(postData.toString());
		osw.flush();
		osw.close();

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'PUT' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	/**
	 * https://developer.veevavault.com/api/18.3/#update-document-version
	 * https://developer.veevavault.com/api/18.3/#update-binder-version
	 *
	 * @return Returns a veevaResponse.
	 */
	public static VeevaResponse updateVersionFields(String sessionId, String host, String apiVersion, String itemId, String version, Map<String, Object> fields)
			throws Exception {

		String urlStr;
		if (StringUtils.isNotBlank(version)) {
			String[] majorMinorVersion = version.split("\\.");
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/documents/" + itemId + "/versions/" + majorMinorVersion[0] + "/" + majorMinorVersion[1];
		} else
			urlStr = "https://" + host + "/api/" + apiVersion + "/objects/documents/" + itemId;
		URL url = new URL(urlStr);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("PUT");
		con.setRequestProperty("Authorization", sessionId);
		con.setRequestProperty("accept", "*/*");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setDoOutput(true);

		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : fields.entrySet()) {
			if (postData.length() != 0) {
				postData.append('&');
			}
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}

		OutputStreamWriter osw = new OutputStreamWriter(
				con.getOutputStream());
		osw.write(postData.toString());
		osw.flush();
		osw.close();

		int responseCode = con.getResponseCode();
		logger.debug("\nSent 'PUT' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);

		return VeevaResponse.getVeevaResponse(con.getInputStream(), con.getHeaderFields());
	}

	private static void addSimpleFormData(String paramName, String wert, PrintWriter body, String crlf) {

		body.append("Content-Disposition: form-data; name=\"")
			.append(paramName)
			.append("\"")
			.append(crlf);
		body.append(crlf);
		body.append(wert);
		body.append(crlf);
		body.flush();
	}

	private static void addFileData(String paramName, String filename, byte[] byteStream, PrintWriter body,
									OutputStream directOutput, String crlf) throws IOException {

		body.append("Content-Disposition: form-data; name=\"")
			.append(paramName)
			.append("\"; filename=\"")
			.append(filename)
			.append("\"")
			.append(crlf)
		body.append("Content-Type: application/octet-stream").append(crlf);
		body.append("Content-Transfer-Encoding: binary").append(crlf);
		body.append(crlf);
		body.flush();

		directOutput.write(byteStream);
		directOutput.flush();

		body.append(crlf);
		body.flush();
	}

	private static void addCloseDelimiter(PrintWriter body, final String boundary, String crlf) {
		body.append("--").append(boundary).append("--").append(crlf);
		body.flush();
	}

	private static void addOpenDelimiter(PrintWriter body, final String boundary, String crlf) {
		body.append("--").append(boundary).append(crlf);

	}

	private static String generateBoundary() {
		StringBuilder buffer = new StringBuilder();
		Random rand = new Random();
		int count = rand.nextInt(11) + 30; // a random size from 30 to 40
		for (int i = 0; i < count; i++) {
			buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
		}
		return buffer.toString();
	}

	/**
	 * Check the veeva response of getVersions and we get the url to be set with the latest version.
	 * @param listIds a JSONArray new JSONObject(new String(veevaResponse.getContentBody())).get("versions")
	 * @return Returns the String to be set in request including the latest version.
	 */
	private static String getUrlWithLatestVersionFromGetVersions(JSONArray listIds) {
		//we have in the veevaResponse the versions of the document/binder
		List<Integer> versionsMajorVersion = new ArrayList<>();
		List<String> versions = new ArrayList<>();

		for (int i = 0; i < listIds.length(); i++) {
			JSONObject jsonObject = (JSONObject) listIds.get(i);
			versionsMajorVersion.add((int) Math.floor(Double.parseDouble(jsonObject.get("number").toString())));
			versions.add(jsonObject.get("number").toString());
		}

		//we get the highest major version
		Integer majorVersion = Collections.max(versionsMajorVersion);
		//we get the highest minor version that has that major version
		Integer minorVersion = 0;
		for(int i=0; i < versionsMajorVersion.size(); i++){
			if(versionsMajorVersion.get(i).equals(majorVersion)){
				minorVersion = Math.max(minorVersion,Integer.parseInt(versions.get(i).substring(versions.get(i).indexOf(".")+1)));
			}
		}

		logger.debug("Latest version retrieved: "+majorVersion+"."+minorVersion);
		JSONObject jsonObject = (JSONObject) listIds.get(versions.indexOf(majorVersion+"."+minorVersion)); //get the highest version
		return jsonObject.get("value").toString();
	}
}
