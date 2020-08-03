package com.docshifter.core.sharepointConnection;

import org.springframework.util.LinkedMultiValueMap;

/**
 * @author Juan Marques created on 29/07/2020
 *
 */
public class HeadersHelper {

	private final AuthTokenHelper tokenHelper;

	public HeadersHelper(AuthTokenHelper tokenHelper) {
		this.tokenHelper = tokenHelper;
	}

	public LinkedMultiValueMap<String, String> getGetHeaders(boolean includeAuthHeader) {
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Cookie", String.join(";", this.tokenHelper.getCookies()));
		headers.add("Accept", "application/json;odata=verbose");
		headers.add("X-ClientService-ClientTag", "SDK-JAVA");
		if (includeAuthHeader) {
			headers.add("Authorization", "Bearer " + this.tokenHelper.getFormDigestValue());
		} else {
			headers.add("X-RequestDigest", this.tokenHelper.getFormDigestValue());
		}
		return headers;
	}

	public LinkedMultiValueMap<String, String> getGetHeadersAsXml(boolean includeAuthHeader) {
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Cookie", String.join(";", this.tokenHelper.getCookies()));
		headers.add("Accept", "application/atom+xml;type=feed;charset=utf-8");
		headers.add("X-ClientService-ClientTag", "SDK-JAVA");
		if (includeAuthHeader) {
			headers.add("Authorization", "Bearer " + this.tokenHelper.getFormDigestValue());
		} else {
			headers.add("X-RequestDigest", this.tokenHelper.getFormDigestValue());
		}
		return headers;
	}

	public LinkedMultiValueMap<String, String> getPostHeaders(String payloadStr) {
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Cookie", String.join(";", this.tokenHelper.getCookies()));
		headers.add("Accept", "application/json;odata=verbose");
		headers.add("Content-Type", "application/json;odata=verbose");
		headers.add("Content-length", "" + payloadStr.getBytes().length);
		headers.add("X-ClientService-ClientTag", "SDK-JAVA");
		headers.add("Authorization", "Bearer " + this.tokenHelper.getFormDigestValue());
		return headers;
	}

	public LinkedMultiValueMap<String, String> getUpdateHeaders(String payloadStr) {
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Cookie", String.join(";", this.tokenHelper.getCookies()));
		headers.add("Accept", "application/json;odata=verbose");
		headers.add("Content-Type", "application/json;odata=verbose");
		headers.add("Content-length", "" + payloadStr.getBytes().length);
		headers.add("X-ClientService-ClientTag", "SDK-JAVA");
		headers.add("X-HTTP-Method", "MERGE");
		headers.add("IF-Match", "*");
		headers.add("Authorization", "Bearer " + this.tokenHelper.getFormDigestValue());
		return headers;
	}

	public LinkedMultiValueMap<String, String> getDeleteHeaders() {
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Cookie", String.join(";", this.tokenHelper.getCookies()));
		headers.add("Accept", "application/json;odata=verbose");
		headers.add("X-ClientService-ClientTag", "SDK-JAVA");
		headers.add("Authorization", "Bearer " + this.tokenHelper.getFormDigestValue());
		headers.add("X-HTTP-Method", "DELETE");
		headers.add("IF-Match", "*");
		return headers;
	}
}