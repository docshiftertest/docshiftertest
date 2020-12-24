package com.docshifter.core.utils.dctm;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;

public class VersionCreator {
	
	
	public static void createVersions(String docbase, String user, String pass, String objectId, int number) throws DfException {
		DocumentumSessionManager dsm = new DocumentumSessionManager(docbase, user, pass);
		
		
		IDfSession session = dsm.getSession();
		
		IDfDocument object = (IDfDocument) session.getObject(new DfId(objectId));
		
		
		for (int i = 0; i < number; i++) {
			System.out.println("version " + i);
			object.checkout();
			System.out.println("checked out ");
			object.checkin(false, "CURRENT");
			System.out.println("checked in ");
			
		}
	}
}
