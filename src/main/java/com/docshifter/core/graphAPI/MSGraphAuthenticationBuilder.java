package com.docshifter.core.graphAPI;

import java.util.Collections;
import java.util.List;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;

/**
 * Class responsible to retrieve microsoft graph session.
 * 
 * @author Juan Marques created on 06/08/2020
 *
 */
public class MSGraphAuthenticationBuilder {

	private static final List<String> SCOPES = Collections.singletonList("https://graph.microsoft.com/.default");

	public static GraphServiceClient<?> createGraphClient(String clientId, String clientSecret, String tenant) {
		return buildClientCredentials(clientId, clientSecret, tenant);
	}

	private static GraphServiceClient<?> buildClientCredentials(String clientId, String clientSecret, String tenant) {

		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(clientId)
				.clientSecret(clientSecret)
				.tenantId(tenant)
				.build();

		final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(SCOPES, clientSecretCredential);

		return GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
	}
}
