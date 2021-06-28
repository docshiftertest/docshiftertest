package com.docshifter.core.utils.veeva;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class VeevaResponse {
	private static final String SUCCESS = "SUCCESS";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String APP_JSON = "application/json";
	public static final String APP_BINARY = "application/octet-stream";
	public static final String CHARSET_MARKER = "charset="; 

	private Map<String, List<String>> headers;
	private byte[] contentBody;
	private String responseStatus;

	public VeevaResponse() {
	}

	/** 
	 * Call this Factory Method if you want to auto-detect good/bad responses...
	 * @param istr InputStream from the connection
	 * @param headers Headers of the connection
	 * @throws IOException if an I/O Exception occurs
	 */
	public static VeevaResponse getVeevaResponse(InputStream istr, Map<String, List<String>> headers) throws IOException {
		return getVeevaResponse(IOUtils.toByteArray(istr), headers);
	}

	/** 
	 * Call this Factory Method if you want to auto-detect good/bad responses...
	 * @param contentBody The content body as a byte[]
	 * @param headers Headers of the connection
	 * @throws IOException if an I/O Exception occurs
	 */
	public static VeevaResponse getVeevaResponse(byte[] contentBody, Map<String, List<String>> headers) throws IOException {
		VeevaResponse veevaResponse = new VeevaResponse();
		veevaResponse.setContentBody(contentBody);
		// Default the Content Type to a sane value... if everything else
		// fails we just have to assume we have a Json Success or Failure
		// message...
		String contentType = APP_JSON;
		if (!headers.containsKey(CONTENT_TYPE)) {
			log.error("Headers from Veeva did not contain a Content-Type! Headers received: {}", dumpHeaders(headers));
		}
		else {
			List<String> contentTypes = headers.get(CONTENT_TYPE);
			if (contentTypes.size() != 1) {
				log.error("Headers from Veeva did not contain one (and only one) Content-Type! Headers received: {}",
						dumpHeaders(headers));
			}
			else {
				contentType = contentTypes.get(0);
				if (StringUtils.isBlank(contentType)) {
					log.error("Headers from Veeva returned a blank Content-Type! Headers received: {}",
							dumpHeaders(headers));
				}
			}
		}
		// Get the charset, if it's specified on the Content-Type
		// This applies to json and octet-stream
		int charsetPos = contentType.indexOf(CHARSET_MARKER);
		String charset = "UTF-8";
		if (charsetPos > -1) {
			charsetPos += CHARSET_MARKER.length();
			charset = contentType.substring(charsetPos);
		}
		String content;
		// Deal with application/json type of response
		if (contentType.toLowerCase().startsWith(APP_JSON)) {
			log.debug("Content-Type is APP_JSON...");
			try {
				content = new String(contentBody, charset);
			}
			catch (UnsupportedEncodingException uxe) {
				log.error("Got Unsupported Encoding Exception getting the content using charset: {}. Will use default" +
						" encoding...", charset, uxe);
				content = new String(contentBody);
			}
			if (SUCCESS.equals(VeevaResponse.getResponseStatus(content))) {
				log.debug("Got SUCCESS!");
				veevaResponse.setHeaders(headers);
				return veevaResponse;
			}
		}
		else {
			if (contentType.toLowerCase().startsWith(APP_BINARY)) {
				log.debug("Content-Type is APP_BINARY...");
				veevaResponse.setHeaders(headers);
				return veevaResponse;
			}
			else {
				log.error("Unrecognised Content-Type: {} received from Veeva", contentType);
			}
		}
		try {
			content = new String(contentBody, charset);
		}
		catch (UnsupportedEncodingException uxe) {
			log.error("Got Unsupported Encoding Exception getting the content using charset: {}. Will use default " +
					"encoding...", charset, uxe);
			content = new String(contentBody);
		}
		VeevaBadResponse badResponse = readFailure(content);
		badResponse.setContentBody(contentBody);
		badResponse.setHeaders(headers);
		log.warn("Returning a VeevaBadResponse, content was: " + content);
		return badResponse;
	}

	/**
	 * Used to check for a good or bad result on a login attempt
	 * @param  responseData
	 * @return VeevaResponse either VeevaLoginSuccess or VeevaBadResponse
	 */
	public static VeevaResponse getLoginResult(String responseData) {
		VeevaResponse result = null;
		String responseStatus = getResponseStatus(responseData);
		log.debug("getLoginResult(), responseStatus: {}", responseStatus);
		if (SUCCESS.equals(responseStatus)) {
			log.debug("getLoginResult() SUCCESS!");
			result = readSuccess(responseData);
		}
		else {
			log.debug("getLoginResult() FAILED!");
			result = readFailure(responseData);
		}
		if (result == null) {
			log.warn("Result was NULL!");
		}
		else {
			log.debug("Result was: {}", result);
		}
		return result;
	}

	private static String getResponseStatus(String responseData) {
		JSONObject jsonObject = new JSONObject(responseData);
		return jsonObject.getString("responseStatus");
	}

	private static VeevaLoginSuccess readSuccess(String responseData) {
		log.debug("Starting readSuccess({})", responseData);
		VeevaLoginSuccess result = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			result = mapper.readValue(responseData, VeevaLoginSuccess.class);
		}
		catch (JsonMappingException jiminy) {
			log.error("Creating Login Success, caught JsonMappingException", jiminy);
			jiminy.printStackTrace();
		}
		catch (JsonParseException jippy) {
			log.error("Creating Login Success, caught JsonParseException", jippy);
			jippy.printStackTrace();
		}
		catch (IOException ioe) {
			log.error("Creating Login Success, caught IOException", ioe);
			ioe.printStackTrace();
		}
		return result;
	}

	private static VeevaBadResponse readFailure(String responseData) {
		log.debug("Starting readFailure({})", responseData);
		VeevaBadResponse result = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			result = mapper.readValue(responseData, VeevaBadResponse.class);
		}
		catch (JsonMappingException jiminy) {
			log.error("Creating Bad Response, caught JsonMappingException", jiminy);
			jiminy.printStackTrace();
		}
		catch (JsonParseException jippy) {
			log.error("Creating Bad Response, caught JsonParseException", jippy);
			jippy.printStackTrace();
		}
		catch (IOException ioe) {
			log.error("Creating Bad Response, caught IOException", ioe);
		}
		return result;
	}

	/**
	 * Used to check a response to see if we got an error, will then try to automatically login
	 * again if we have an expired session id
	 * @param veevaResponse The response, normally from getVeevaResponse which could be a VeevaBadResponse
	 * @param sessionId the current session id
	 * @param host The Veeva host
	 * @param apiVersion The Veeva api version, e.g. v18.3
	 * @param veevaUser The user to login to Veeva
	 * @param veevaPass The Veeva password
	 * @return String The session id, if success. else null
	 * @throws Exception if an Exception occurs
	 */
	public static String checkResponse(VeevaResponse veevaResponse, String sessionId, String host, String apiVersion,
                                       String veevaUser, String veevaPass) throws Exception {
		log.debug("Into checkResponse...with Veeva Response: {}, Veeva User: {} and Veeva Password length: {}",
				veevaResponse, veevaUser, (veevaPass == null) ? "NULL!" : veevaPass.length());
		String responseStatus = SUCCESS;
		if (veevaResponse instanceof VeevaBadResponse) {
			log.debug("It's a BAD Response, go stand in the corner!");
			// See if we have an expired session id
			VeevaBadResponse badResponse  = (VeevaBadResponse) veevaResponse;
			if (badResponse.getErrors() != null &&
					badResponse.getErrors().size() == 1 &&
					badResponse.getErrors().get(0).getType().equalsIgnoreCase("INVALID_SESSION_ID")) {
				log.debug("Got invalid session id, so retrieving new Session ID...");
				// Obtain NewSesion
				VeevaResponse veevaSessionResponse = Requests.getSession(host, apiVersion, veevaUser, veevaPass);
				// We know this will always return the Veeva response as Json as we're not asking for file content!
				String responseStr = new String(veevaSessionResponse.getContentBody());
				responseStatus = getResponseStatus(responseStr);
				if (!SUCCESS.equals(responseStatus)) {
					badResponse  = readFailure(responseStr);
					if (badResponse.getErrors() != null &&
							badResponse.getErrors().size() == 1 &&
							badResponse.getErrors().get(0).getType().toString().equalsIgnoreCase("USERNAME_OR_PASSWORD_INCORRECT")) {
						throw new Exception("Error retrieving session=> AUTHENTICATION_FAILED:" + responseStr);
					}
					throw new Exception("Error when retrieving session again.., Response received: [" + responseStr + "]");
				}
				else {
					VeevaLoginSuccess goodResponse = readSuccess(responseStr);
					log.debug("Returning goodResponse Session Id: {}...",
							goodResponse.getSessionId().substring(0, 10));
					return goodResponse.getSessionId();
				}
			}
			else {
				throw new Exception("Error when doing request, Response received:" + veevaResponse.toString());
			}
		}
		log.debug("At end, returning Session Id: {}...",
				sessionId.substring(0, 10));
		return sessionId;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

	public Map<String, List<String>> getHeaders() {
		if (headers == null) {
			headers = new HashMap<>(); 
		}
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public byte[] getContentBody() throws IOException {
		return this.contentBody;
	}

	public void setContentBody(byte[] contentBody) throws IOException {
		this.contentBody = contentBody;
	}

	/**
	 * Give a nice representation of this class for debugging
	 */
	@Override
	public String toString() {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("Response Status: ");
		sBuf.append(this.responseStatus);
		sBuf.append(", ");
		sBuf.append("Content Body: ");
		if (this.contentBody == null) {
			sBuf.append("null");
		}
		else {
			sBuf.append(new String(this.contentBody));
		}
		sBuf.append(", ");
		sBuf.append(dumpHeaders(this.getHeaders()));
		return sBuf.toString();
	}

	private static String dumpHeaders(Map<String, List<String>> headers) {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("Headers: {");
		for (String header : headers.keySet()) {
			sBuf.append(header);
			sBuf.append("=");
			List<String> values = headers.get(header);
			for (String value : values) {
				sBuf.append(value);
				sBuf.append(", ");
			}
			if (values.size() > 0) {
				sBuf.setLength(sBuf.length() - 2);
			}
			sBuf.append(System.lineSeparator());
		}
		sBuf.append("}");
		return sBuf.toString();
	}
}
