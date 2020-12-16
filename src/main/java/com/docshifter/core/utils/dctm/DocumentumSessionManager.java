package com.docshifter.core.utils.dctm;


import com.documentum.com.DfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;

/**
 * Implementation class of the ISessionManager interface
 *
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */
public class DocumentumSessionManager implements ISessionManager<IDfSession> {

	private String docbase = "";
	private String user = "";
	private String pass = "";

	private DfClientX clientx;
	private IDfSessionManager sMgr = null;

	public DocumentumSessionManager(String docbase, String user, String pass)
			throws DfException {
		// Init the instance attributes
		this.docbase = docbase;
		this.user = user;
		this.pass = pass;

		//create Client objects
		clientx = new DfClientX();
		IDfClient client = clientx.getLocalClient();

		//create a Session Manager object
		sMgr = client.newSessionManager();

		//create an IDfLoginInfo object named loginInfoObj
		IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
		loginInfoObj.setUser(this.user);
		loginInfoObj.setPassword(this.pass);
		loginInfoObj.setDomain(null);

		//bind the Session Manager to the login info
		sMgr.setIdentity(this.docbase, loginInfoObj);
	}

	public DfClientX getClientx() {
		return clientx;
	}

	public String getDocbase() {
		return docbase;
	}

	public void setDocbase(String docbase) {
		this.docbase = docbase;
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

	public DctmSession getSession() throws DfException{
		return new DctmSession(sMgr.newSession(docbase));
	}

	public void release(IDfSession session) throws DfException {
		if (session instanceof DctmSession) {
			sMgr.release(((DctmSession)session).getSession());
		} else {
			sMgr.release(session);
		}
	}
}
