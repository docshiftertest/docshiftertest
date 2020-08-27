package com.docshifter.core.graphAPI;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.microsoft.graph.auth.confidentialClient.ClientCredentialProvider;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;

/**
 * Class responsible to retrieve microsoft graph session.
 * 
 * @author Juan Marques created on 06/08/2020
 *
 */
public class MSGraphAuthenticationBuilder {

	private static final List<String> SCOPES = Collections
			.unmodifiableList(Collections.singletonList("https://graph.microsoft.com/.default"));

	public static IGraphServiceClient createGraphClient(String clientId, String clientSecret, String tenant,
			NationalCloud nationalCloud) {
		return buildClientCredentials(clientId, clientSecret, tenant, nationalCloud);
	}

	private static IGraphServiceClient buildClientCredentials(String clientId, String clientSecret, String tenant,
			NationalCloud nationalCloud) {

		ClientCredentialProvider cliCredential = new ClientCredentialProvider(clientId, SCOPES, clientSecret, tenant,
				nationalCloud);

		return GraphServiceClient.builder().authenticationProvider(cliCredential).buildClient();

	}
}
