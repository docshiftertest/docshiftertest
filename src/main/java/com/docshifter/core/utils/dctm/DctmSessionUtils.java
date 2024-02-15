package com.docshifter.core.utils.dctm;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfServiceException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfEnumeration;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.acs.IDfAcsRequest;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;
import lombok.extern.log4j.Log4j2;

/**
 * Created by michiel.vandriessche@docbyte.com on 3/10/16.
 */
@Log4j2
public class DctmSessionUtils {
	
	private IDfClient client;
	private IDfClientX clientX;
	
	
	private static DctmSessionUtils instance = null;
	
	
	public static DctmSessionUtils getInstance() {
		if (instance == null) {
			instance = new DctmSessionUtils();
		}
		return instance;
	}
	
	
	private DctmSessionUtils() {
		
		try {
			clientX = new DfClientX();
			client = clientX.getLocalClient();
			
		} catch (Exception e) {
			log.error("Failed to load client", e);
		}
	}
	
	public IDfClient getClient() {
		return client;
	}
	
	public IDfClientX getClientX() {
		return clientX;
	}
	
	public DctmSession createSession(String repo, String user, String pass) throws DfServiceException {

		IDfSessionManager sessionManager = client.newSessionManager();
		IDfLoginInfo loginInfoObj = clientX.getLoginInfo();

		loginInfoObj.setUser(user);
		loginInfoObj.setPassword(pass);

		sessionManager.setIdentity(repo, loginInfoObj);

		return new DctmSession(sessionManager.newSession(repo));
		
		
	}
	
	public DctmSession createSession(DctmConnectionDetails details) throws DfServiceException {
		return this.createSession(
				details.getRepository(),
				details.getUsername(),
				details.getPassword()
		);
	}
	
	public boolean authenticate(String repo, String user, String pass) {
		try {
			IDfSessionManager sessionManager = client.newSessionManager();
			IDfLoginInfo loginInfoObj = clientX.getLoginInfo();
			
			loginInfoObj.setUser(user);
			loginInfoObj.setPassword(pass);
			
			sessionManager.setIdentity(repo, loginInfoObj);
			
			sessionManager.authenticate(repo);
			return true;
		} catch (Exception exception) {
			log.info("Failed to login", exception);
			return false;
		}
	}
	
	
	public String getAcsURL(IDfSysObject obj) throws DfException {
		
		IDfEnumeration acsRequests = obj.getAcsRequests(obj.getFormat().getName(), 0, "", clientX.getAcsTransferPreferences());
		
		while (acsRequests.hasMoreElements()) {
			IDfAcsRequest acsRequest = (IDfAcsRequest) acsRequests.nextElement();
			String acsUrl = acsRequest.makeURL();
			return acsUrl;
		}
		return null;
	}
	
	
}
