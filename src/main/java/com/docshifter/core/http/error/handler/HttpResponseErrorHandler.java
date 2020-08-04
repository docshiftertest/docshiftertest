package com.docshifter.core.http.error.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * 
 * Handling http request error.
 * @author Juan Marques created on 04/08/2020
 *
 */
public class HttpResponseErrorHandler extends DefaultResponseErrorHandler {

	private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return errorHandler.hasError(response);
	}

	public void handleError(ClientHttpResponse response) throws IOException {
		String responseBody = response.getBody().toString();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("code", response.getStatusCode().toString());
		properties.put("body", responseBody);
		properties.put("header", response.getHeaders());
	}

}
