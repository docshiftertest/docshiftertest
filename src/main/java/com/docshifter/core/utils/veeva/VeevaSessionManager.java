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
		String urlStr = "https://" + host + "/api/" + API_VERSION + "/auth";
		URL url = new URL(urlStr);
		
		Map<String,Object> params = new LinkedHashMap<>();
		params.put("username", user);
		log.debug("Using username: {} and password with length: {}",
				user, pass.length());
		params.put("password", pass);
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String,Object> param : params.entrySet()) {
			if (postData.length() != 0) {
					postData.append('&');
			}
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			//postData.append(String.valueOf(param.getValue()));
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
		log.info("Sent 'POST' request to URL: {}", url);
		log.info("Post parameters: {}", new String(postDataBytes));
		log.info("Response Code: {}", responseCode);
	 
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
		//getBinders(jsonObj.get("sessionId").toString());
		//getSpecificBinders(jsonObj.get("sessionId").toString(), "SELECT id FROM documents WHERE binder__v=true");
		return new VeevaSession("", jsonObj.get("sessionId").toString(), Instant.now());
	}
}
