package com.docshifter.core.graphAPI;

import com.docshifter.core.graphAPI.integration.Sharepoint;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.Getter;

/**
 *
 * @author Juan Marques created on 06/08/2020
 * @apiNote To use beta api please set
 *          graphClient.setServiceRoot("https://graph.microsoft.com/beta");
 */
@Getter
public class GraphClient {

	private final Sharepoint sharepoint;

	private final GraphServiceClient<?> rawGraphClient;

	public GraphClient(GraphServiceClient<?> graphClient) {
		this.sharepoint = new Sharepoint(graphClient);
		this.rawGraphClient = graphClient;
	}
}
