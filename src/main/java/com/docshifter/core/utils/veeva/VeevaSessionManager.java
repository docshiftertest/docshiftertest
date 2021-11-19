package com.docshifter.core.utils.veeva;

import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation class of the ISessionManager interface
 *
 * @author Janire Fernandez
 *
 */
@Log4j2
public class VeevaSessionManager implements ISessionManager<VeevaSession> {

	private static final String API_VERSION = "v18.3";
	private String host = "";
	private String user = "";
	private String pass = "";

	public VeevaSessionManager(String host, String user, String pass) {
		this.host = host;
		this.user = user;
		this.pass = pass;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public VeevaSession getSession() throws Exception {
		HttpURLConnection con = Requests.postSessionRequest(host, API_VERSION, user, pass);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();
	
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		log.info(response.toString());

		JSONObject jsonObj = new JSONObject(response.toString());
		return new VeevaSession(host, jsonObj.get("sessionId").toString(), Instant.now());
	}
}
