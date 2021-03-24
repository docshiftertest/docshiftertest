package com.docshifter.core.graphAPI;

import com.docshifter.core.graphAPI.integration.Sharepoint;
import com.microsoft.graph.requests.GraphServiceClient;

/**
 *
 * @author Juan Marques created on 06/08/2020
 * @apiNote To use beta api please set
 *          graphClient.setServiceRoot("https://graph.microsoft.com/beta");
 */
public class GraphClient {

	private final Sharepoint sharepoint;

	public GraphClient(GraphServiceClient<?> graphClient) {
		this.sharepoint = new Sharepoint(graphClient);
	}

	public Sharepoint getSharepoint() {
		return sharepoint;
	}

}
