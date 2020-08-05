package com.docshifter.core.sharepointConnection;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Juan Marques created on 29/07/2020
 *
 *         Helper class to manage login against SharepointOnline and retrieve
 *         auth token and cookies to Retrieves all info needed to set auth
 *         headers to call Sharepoint Rest API v1.
 */
public class AuthTokenHelper {

	private static final Logger logger = LoggerFactory.getLogger(AuthTokenHelper.class);
	private final String spSiteUri;
	private String formDigestValue;
	private final String domain;
	private List<String> cookies;
	private String tokenExpirationDate;
	private String payload = "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\"\n"
			+ "      xmlns:a=\"http://www.w3.org/2005/08/addressing\"\n"
			+ "      xmlns:u=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n"
			+ "  <s:Header>\n"
			+ "    <a:Action s:mustUnderstand=\"1\">http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue</a:Action>\n"
			+ "    <a:ReplyTo>\n" + "      <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>\n"
			+ "    </a:ReplyTo>\n"
			+ "    <a:To s:mustUnderstand=\"1\">https://login.microsoftonline.com/extSTS.srf</a:To>\n"
			+ "    <o:Security s:mustUnderstand=\"1\"\n"
			+ "       xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n"
			+ "      <o:UsernameToken>\n" + "        <o:Username>%s</o:Username>\n"
			+ "        <o:Password>%s</o:Password>\n" + "      </o:UsernameToken>\n" + "    </o:Security>\n"
			+ "  </s:Header>\n" + "  <s:Body>\n"
			+ "    <t:RequestSecurityToken xmlns:t=\"http://schemas.xmlsoap.org/ws/2005/02/trust\">\n"
			+ "      <wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n"
			+ "        <a:EndpointReference>\n" + "          <a:Address>%s</a:Address>\n"
			+ "        </a:EndpointReference>\n" + "      </wsp:AppliesTo>\n"
			+ "      <t:KeyType>http://schemas.xmlsoap.org/ws/2005/05/identity/NoProofKey</t:KeyType>\n"
			+ "      <t:RequestType>http://schemas.xmlsoap.org/ws/2005/02/trust/Issue</t:RequestType>\n"
			+ "      <t:TokenType>urn:oasis:names:tc:SAML:1.0:assertion</t:TokenType>\n"
			+ "    </t:RequestSecurityToken>\n" + "  </s:Body>\n" + "</s:Envelope>";

	private final RestTemplate restTemplate;

	public AuthTokenHelper(RestTemplate restTemplate, String user, String passwd, String domain, String spSiteUri) {
		this.restTemplate = restTemplate;
		this.domain = domain;
		this.spSiteUri = spSiteUri;
		this.payload = String.format(this.payload, user, passwd, domain);
	}

	public void init() throws Exception {
		String securityToken = receiveSecurityToken();
		this.cookies = getSignInCookies(securityToken);
		formDigestValue = getFormDigestValue(this.cookies);
	}

	protected String receiveSecurityToken() throws URISyntaxException {
		String TOKEN_LOGIN_URL = "https://login.microsoftonline.com/extSTS.srf";
		RequestEntity<String> requestEntity = new RequestEntity<>(this.payload, HttpMethod.POST,
				new URI(TOKEN_LOGIN_URL));

		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
		String securityToken = responseEntity.getBody();
		String tokenKey1 = "<wsse:BinarySecurityToken";
		String tokenKey2 = "</wsse:BinarySecurityToken>";

		securityToken = securityToken.substring(securityToken.indexOf(tokenKey1));
		securityToken = securityToken.substring(securityToken.indexOf(">") + 1);
		securityToken = securityToken.substring(0, securityToken.indexOf(tokenKey2));

		this.tokenExpirationDate = responseEntity.getBody();

		String lifeTimeKey1 = "<wst:Lifetime>";
		String lifeTimeKey2 = "</wst:Lifetime>";
		this.tokenExpirationDate = this.tokenExpirationDate.substring(this.tokenExpirationDate.indexOf(lifeTimeKey1));
		this.tokenExpirationDate = this.tokenExpirationDate.substring(this.tokenExpirationDate.indexOf(">") + 1);
		this.tokenExpirationDate = this.tokenExpirationDate.substring(0,
				this.tokenExpirationDate.indexOf(lifeTimeKey2));

		return securityToken;
	}

	/**
	 * Check the token validation.
	 * @return true if the token has expired otherwise false.
	 */
	public boolean isTokenExpired() {

		Map<String, String> createdAndExpireDate = getCreatedAndExpiresDate();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
				.withZone(ZoneId.systemDefault());

		String createdDateString = createdAndExpireDate.get("createdDate");
		String expiresDateString = createdAndExpireDate.get("expiresDate");
		
		ZonedDateTime createdDate = ZonedDateTime.of(LocalDateTime.parse(createdDateString, formatter),
				ZoneId.systemDefault());
		
		ZonedDateTime expiresDate = ZonedDateTime.of(LocalDateTime.parse(expiresDateString, formatter),
				ZoneId.systemDefault());

		Instant instant = Instant.now();
		
		Instant expiresIntant = Instant.parse(expiresDateString);		
		logger.debug("Token duration: " + ChronoUnit.SECONDS.between(createdDate, expiresDate) + " seconds");
		
		long tokenExpiresIn = ChronoUnit.SECONDS.between(instant, Instant.parse(expiresDateString));
		logger.debug("Token will expires in: " + tokenExpiresIn + " seconds");

		if (instant.isAfter(expiresIntant)) {
			logger.debug("Token is expired....");
			return true;
		}

		return false;

	}

	/**
	 * Method to filter created and expires dates from response body
	 * 
	 * @return Map<String,String> with created and expires date
	 */
	private Map<String, String> getCreatedAndExpiresDate() {

		Map<String, String> createdAndExpireDate = new HashMap<>();

		String createdDate = StringUtils.EMPTY;
		String expiresDate = StringUtils.EMPTY;
		String createdDateKey1 = "<wsu:Created>";
		String createdDateKey2 = "</wsu:Created>";
		String expiresDateKey1 = "<wsu:Expires>";
		String expiresDateKey2 = "</wsu:Expires>";

		createdDate = this.tokenExpirationDate.substring(this.tokenExpirationDate.indexOf(createdDateKey1));
		createdDate = createdDate.substring(createdDate.indexOf(">") + 1);
		createdDate = createdDate.substring(0, createdDate.indexOf(createdDateKey2));

		expiresDate = this.tokenExpirationDate.substring(this.tokenExpirationDate.indexOf(expiresDateKey1));
		expiresDate = expiresDate.substring(expiresDate.indexOf(">") + 1);
		expiresDate = expiresDate.substring(0, expiresDate.indexOf(expiresDateKey2));

		createdAndExpireDate.put("createdDate", createdDate);
		createdAndExpireDate.put("expiresDate", expiresDate);

		return createdAndExpireDate;

	}

	protected List<String> getSignInCookies(String securityToken) throws Exception {
		RequestEntity<String> requestEntity = new RequestEntity<>(securityToken, HttpMethod.POST,
				new URI(String.format("https://%s/_forms/default.aspx?wa=wsignin1.0", this.domain)));

		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
		HttpHeaders headers = responseEntity.getHeaders();
		List<String> cookies = headers.get("Set-Cookie");

		if (CollectionUtils.isEmpty(cookies)) {
			throw new Exception("Unable to sign in: no cookies returned in response");
		}
		return cookies;
	}

	protected String getFormDigestValue(List<String> cookies) throws URISyntaxException, JSONException {

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Cookie", String.join(";", cookies));
		headers.add("Accept", "application/json;odata=verbose");
		headers.add("X-ClientService-ClientTag", "SDK-JAVA");

		RequestEntity<String> requestEntity = new RequestEntity<>(headers, HttpMethod.POST,
				new URI(String.format("https://%s/_api/contextinfo", this.domain)));

		ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
		String body = responseEntity.getBody();
		JSONObject json = new JSONObject(body);

		return json.getJSONObject("d").getJSONObject("GetContextWebInformation").getString("FormDigestValue");
	}

	/**
	 * The security token to use in Authorization Bearer header or X-RequestDigest
	 * header (depending on operation called from Rest API).
	 *
	 */
	public String getFormDigestValue() {
		return formDigestValue;
	}

	/**
	 * Retrieves session cookies to use in communication with the Sharepoint Online
	 * Rest API.
	 *
	 */
	public List<String> getCookies() {
		return this.cookies;
	}

	/**
	 * Mounts the sharepoint online site url, composed by the protocol, domain and
	 * spSiteUri.
	 *
	 */
	public URI getSharepointSiteUrl(String apiPath) throws URISyntaxException {
		return new URI("https", this.domain, this.spSiteUri + apiPath, null);
	}

	/**
	 * Mounts the sharepoint online site url with params, composed by the protocol,
	 * domain and spSiteUri
	 *
	 */
	public URI getSharepointSiteUrlWithParam(String apiPath, String parameterName, String parameterValue)
			throws URISyntaxException {

		String url = getSharepointSiteUrl(apiPath).toString();

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam(parameterName, parameterValue);
		UriComponents components = builder.build(false);

		return components.toUri();
	}

}