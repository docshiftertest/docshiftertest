package com.docshifter.core.utils.veeva;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Instant;

@Log4j2
public class VeevaSession implements ISession {
	
	private String sessionID;
	private Instant acquisitionTime;
	private String host;
	
	public VeevaSession(String response) throws JsonMappingException, JsonParseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		VeevaLoginSuccess veevaResponse = mapper.readValue(response, VeevaLoginSuccess.class);
		log.info(veevaResponse.toString());
	}
	
	public VeevaSession(String host, String sessionId, Instant acquisitionTime) {
		this.host = host;
		this.sessionID = sessionId;
		this.acquisitionTime = acquisitionTime;
	}

	@Override
	public String getSessionID() {
		return this.sessionID;
	}

	@Override
	public Instant getAcquisitionTime() {
		return this.acquisitionTime;
	}

	@Override
	public String getHost() {
		return this.host;
	}
}
