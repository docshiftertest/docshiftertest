package com.docshifter.core.utils.dctm;


import com.documentum.fc.client.DormantStatus;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfAuditTrailManager;
import com.documentum.fc.client.IDfBatchManager;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfEnumeration;
import com.documentum.fc.client.IDfEventManager;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfGetObjectOptions;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfLocalModuleRegistry;
import com.documentum.fc.client.IDfLocalTransaction;
import com.documentum.fc.client.IDfObjectPathsMap;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfRelationType;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSessionScopeManager;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.client.IDfVersionTreeLabels;
import com.documentum.fc.client.IDfWorkflowBuilder;
import com.documentum.fc.client.acs.IDfAcsTransferPreferences;
import com.documentum.fc.client.fulltext.IDfFtConfig;
import com.documentum.fc.client.mq.IDfMessageQueueManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Log4j2
public class DctmSession implements AutoCloseable, IDfSession {
	
	private IDfSession session;
	
	DctmSession(IDfSession session) {
		this.session = session;
	}
	
	
	@Override
	public void close() {
		
		try {
			if (session != null) {
				session.getSessionManager().release(session);
			}
		} catch (Exception e) {
			log.error("Failed to release session", e);
		}
	}
	
	public IDfSession getSession() {
		return session;
	}
	
	@Override
	public boolean isConnected() {
		return session.isConnected();
	}
	
	@Override
	public boolean isShared() throws DfException {
		return session.isShared();
	}
	
	@Override
	public void disconnect() throws DfException {
		session.disconnect();
	}
	
	@Override
	public boolean isTransactionActive() throws DfException {
		return session.isTransactionActive();
	}
	
	@Override
	public void beginTrans() throws DfException {
		session.beginTrans();
	}
	
	@Override
	public void abortTrans() throws DfException {
		session.abortTrans();
	}
	
	@Override
	public void commitTrans() throws DfException {
		session.commitTrans();
	}
	
	@Override
	public IDfLocalTransaction beginTransEx() throws DfException {
		return session.beginTransEx();
	}
	
	@Override
	public void commitTransEx(IDfLocalTransaction iDfLocalTransaction) throws DfException {
		session.commitTransEx(iDfLocalTransaction);
	}
	
	@Override
	public void abortTransEx(IDfLocalTransaction iDfLocalTransaction) throws DfException {
		session.abortTransEx(iDfLocalTransaction);
	}
	
	@Override
	public IDfBatchManager getBatchManager() {
		return session.getBatchManager();
	}
	
	@Override
	public void authenticate(IDfLoginInfo iDfLoginInfo) throws DfException {
		session.authenticate(iDfLoginInfo);
	}
	
	@Override
	public IDfSessionManager getSessionManager() {
		return session.getSessionManager();
	}
	
	@Override
	public IDfTypedObject getSessionConfig() throws DfException {
		return session.getSessionConfig();
	}
	
	@Override
	public IDfTypedObject getConnectionConfig() throws DfException {
		return session.getConnectionConfig();
	}
	
	@Override
	public IDfTypedObject getDocbaseConfig() throws DfException {
		return session.getDocbaseConfig();
	}
	
	@Override
	public IDfTypedObject getServerConfig() throws DfException {
		return session.getServerConfig();
	}
	
	@Override
	public String getSessionId() throws DfException {
		return session.getSessionId();
	}
	
	@Override
	public IDfLoginInfo getLoginInfo() throws DfException {
		return session.getLoginInfo();
	}
	
	@Override
	public IDfSession getRelatedSession(String s) throws DfException {
		return session.getRelatedSession(s);
	}
	
	@Override
	public IDfSession getRelatedSession(IDfId iDfId) throws DfException {
		return session.getRelatedSession(iDfId);
	}
	
	@Override
	public String getLoginUserName() throws DfException {
		return session.getLoginUserName();
	}
	
	@Override
	public String getDocbaseId() throws DfException {
		return session.getDocbaseId();
	}
	
	@Override
	public String getDocbaseName() throws DfException {
		return session.getDocbaseName();
	}
	
	@Override
	public String getServerVersion() throws DfException {
		return session.getServerVersion();
	}
	
	@Override
	public String getDocbaseOwnerName() throws DfException {
		return session.getDocbaseOwnerName();
	}
	
	@Override
	public String getDBMSName() throws DfException {
		return session.getDBMSName();
	}
	
	@Override
	public String getSecurityMode() throws DfException {
		return session.getSecurityMode();
	}
	
	@Override
	public int getDefaultACL() throws DfException {
		return session.getDefaultACL();
	}
	
	@Override
	public boolean isACLDocbase() throws DfException {
		return session.isACLDocbase();
	}
	
	@Override
	public IDfPersistentObject getObject(IDfId iDfId) throws DfException {
		return session.getObject(iDfId);
	}
	
	@Override
	public IDfPersistentObject getObjectWithOptions(IDfId iDfId, IDfGetObjectOptions iDfGetObjectOptions) throws DfException {
		return session.getObjectWithOptions(iDfId, iDfGetObjectOptions);
	}
	
	@Override
	public IDfPersistentObject newObject(String s) throws DfException {
		return session.newObject(s);
	}
	
	@Override
	public IDfPersistentObject newLightObject(String s, IDfId iDfId) throws DfException {
		return session.newLightObject(s, iDfId);
	}
	
	@Override
	public IDfPersistentObject getObjectWithCaching(IDfId iDfId, String s, String s1, String s2, boolean b, boolean b1) throws DfException {
		return session.getObjectWithCaching(iDfId, s, s1, s2, b, b1);
	}
	
	@Override
	public IDfId getIdByQualification(String s) throws DfException {
		return session.getIdByQualification(s);
	}
	
	@Override
	public IDfPersistentObject getObjectByQualification(String s) throws DfException {
		return session.getObjectByQualification(s);
	}
	
	@Override
	public IDfEnumeration getObjectsByQuery(String s, String s1) throws DfException {
		return session.getObjectsByQuery(s, s1);
	}
	
	@Override
	public IDfPersistentObject getObjectByPath(String s) throws DfException {
		return session.getObjectByPath(s);
	}
	
	@Override
	public IDfFolder getFolderByPath(String s) throws DfException {
		return session.getFolderByPath(s);
	}
	
	@Override
	public IDfFolder getFolderBySpecification(String s) throws DfException {
		return session.getFolderBySpecification(s);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public IDfPersistentObject newObjectWithType(String s, String s1) throws DfException {
		return session.newObjectWithType(s, s1);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public IDfPersistentObject getObjectWithType(IDfId iDfId, String s, String s1) throws DfException {
		return session.getObjectWithType(iDfId, s, s1);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public IDfPersistentObject getObjectWithInterface(IDfId iDfId, String s) throws DfException {
		return session.getObjectWithInterface(iDfId, s);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public IDfPersistentObject getObjectByQualificationWithInterface(String s, String s1) throws DfException {
		return session.getObjectByQualificationWithInterface(s, s1);
	}
	
	@Override
	public IDfPersistentObject getReplicaForMaster(IDfId iDfId) throws DfException {
		return session.getReplicaForMaster(iDfId);
	}
	
	@Override
	public void flush(String s, String s1) throws DfException {
		session.flush(s, s1);
	}
	
	@Override
	public void flushEx(String s, String s1, boolean b, boolean b1) throws DfException {
		session.flushEx(s, s1, b, b1);
	}
	
	@Override
	public void flushCache(boolean b) throws DfException {
		session.flushCache(b);
	}
	
	@Override
	public void flushGlobalCache(String s) throws DfException {
		session.flushGlobalCache(s);
	}
	
	@Override
	public IDfACL getACL(String s, String s1) throws DfException {
		return session.getACL(s, s1);
	}
	
	@Override
	public IDfFormat getFormat(String s) throws DfException {
		return session.getFormat(s);
	}
	
	@Override
	public IDfType getType(String s) throws DfException {
		return session.getType(s);
	}
	
	@Override
	public IDfTypedObject getTypeDescription(String s, String s1, IDfId iDfId, String s2) throws DfException {
		return session.getTypeDescription(s, s1, iDfId, s2);
	}
	
	@Override
	public IDfGroup getGroup(String s) throws DfException {
		return session.getGroup(s);
	}
	
	@Override
	public IDfUser getUser(String s) throws DfException {
		return session.getUser(s);
	}
	
	@Override
	public IDfUser getUserByOSName(String s, String s1) throws DfException {
		return session.getUserByOSName(s, s1);
	}
	
	@Override
	public IDfUser getUserByLoginName(String s, String s1) throws DfException {
		return session.getUserByLoginName(s, s1);
	}
	
	@Override
	public String getLoginTicket() throws DfException {
		return session.getLoginTicket();
	}
	
	@Override
	public String getLoginTicketEx(String s, String s1, int i, boolean b, String s2) throws DfException {
		return session.getLoginTicketEx(s, s1, i, b, s2);
	}
	
	@Override
	public String getLoginTicketForUser(String s) throws DfException {
		return session.getLoginTicketForUser(s);
	}
	
	@Override
	public String getAliasSet() throws DfException {
		return session.getAliasSet();
	}
	
	@Override
	public void setAliasSet(String s) throws DfException {
		session.setAliasSet(s);
	}
	
	@Override
	public String resolveAlias(IDfId iDfId, String s) throws DfException {
		return session.resolveAlias(iDfId, s);
	}
	
	@Override
	public String getMessage(int i) throws DfException {
		return session.getMessage(i);
	}
	
	@Override
	public IDfCollection getLastCollection() throws DfException {
		return session.getLastCollection();
	}
	
	@Override
	public void setBatchHint(int i) throws DfException {
		session.setBatchHint(i);
	}
	
	@Override
	public IDfCollection apply(String s, String s1, IDfList iDfList, IDfList iDfList1, IDfList iDfList2) throws DfException {
		return session.apply(s, s1, iDfList, iDfList1, iDfList2);
	}
	
	@Override
	public String describe(String s, String s1) throws DfException {
		return session.describe(s, s1);
	}
	
	@Override
	public IDfId archive(String s, String s1, int i, boolean b, IDfTime iDfTime) throws DfException {
		return session.archive(s, s1, i, b, iDfTime);
	}
	
	@Override
	public IDfId restore(String s, String s1, String s2, int i, boolean b, IDfTime iDfTime) throws DfException {
		return session.restore(s, s1, s2, i, b, iDfTime);
	}
	
	@Override
	public void changePassword(String s, String s1) throws DfException {
		session.changePassword(s, s1);
	}
	
	@Override
	public void purgeLocalFiles() throws DfException {
		session.purgeLocalFiles();
	}
	
	@Override
	public void reInit(String s) throws DfException {
		session.reInit(s);
	}
	
	@Override
	public void reInitEx(String s, boolean b) throws DfException {
		session.reInitEx(s, b);
	}
	
	@Override
	public void reStart(String s, boolean b) throws DfException {
		session.reStart(s, b);
	}
	
	@Override
	public void shutdown(boolean b, boolean b1) throws DfException {
		session.shutdown(b, b1);
	}
	
	@Override
	public void dequeue(IDfId iDfId) throws DfException {
		session.dequeue(iDfId);
	}
	
	@Override
	public void dequeueAll() throws DfException {
		session.dequeueAll();
	}
	
	@Override
	public boolean hasEvents() throws DfException {
		return session.hasEvents();
	}
	
	@Override
	public IDfCollection getEvents() throws DfException {
		return session.getEvents();
	}
	
	@Override
	public IDfCollection getTasks(String s, int i, String s1, String s2) throws DfException {
		return session.getTasks(s, i, s1, s2);
	}
	
	@Override
	public IDfCollection getTasksEx(String s, int i, IDfList iDfList, IDfList iDfList1) throws DfException {
		return session.getTasksEx(s, i, iDfList, iDfList1);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public IDfId sendToDistributionList(IDfList iDfList, IDfList iDfList1, String s, IDfList iDfList2, int i, boolean b) throws DfException {
		return session.sendToDistributionList(iDfList, iDfList1, s, iDfList2, i, b);
	}
	
	@Override
	public IDfId sendToDistributionListEx(IDfList iDfList, IDfList iDfList1, String s, IDfList iDfList2, int i, int i1) throws DfException {
		return session.sendToDistributionListEx(iDfList, iDfList1, s, iDfList2, i, i1);
	}
	
	@Override
	public IDfCollection getRunnableProcesses(String s) throws DfException {
		return session.getRunnableProcesses(s);
	}
	
	@Override
	public IDfWorkflowBuilder newWorkflowBuilder(IDfId iDfId) throws DfException {
		return session.newWorkflowBuilder(iDfId);
	}
	
	@Override
	public IDfVersionTreeLabels getVersionTreeLabels(IDfId iDfId) throws DfException {
		return session.getVersionTreeLabels(iDfId);
	}
	
	@Override
	public IDfRelationType getRelationType(String s) throws DfException {
		return session.getRelationType(s);
	}
	
	@Override
	public IDfClient getClient() {
		return session.getClient();
	}
	
	@Override
	public IDfTypedObject getClientConfig() throws DfException {
		return session.getClientConfig();
	}
	
	@Override
	public IDfTypedObject getDocbrokerMap() throws DfException {
		return session.getDocbrokerMap();
	}
	
	@Override
	public IDfTypedObject getServerMap(String s) throws DfException {
		return session.getServerMap(s);
	}
	
	@Override
	public IDfAuditTrailManager getAuditTrailManager() throws DfException {
		return session.getAuditTrailManager();
	}
	
	@Override
	public IDfEventManager getEventManager() throws DfException {
		return session.getEventManager();
	}
	
	@Override
	public String getDocbaseScope() throws DfException {
		return session.getDocbaseScope();
	}
	
	@Override
	public String setDocbaseScope(String s) throws DfException {
		return session.setDocbaseScope(s);
	}
	
	@Override
	public String setDocbaseScopeById(IDfId iDfId) throws DfException {
		return session.setDocbaseScopeById(iDfId);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public boolean isAdopted() throws DfException {
		return session.isAdopted();
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public boolean isRemote() throws DfException {
		return session.isRemote();
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public boolean lock(int i) {
		return session.lock(i);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public boolean unlock() {
		return session.unlock();
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public String getDMCLSessionId() throws DfException {
		return session.getDMCLSessionId();
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public void traceDMCL(int i, String s) throws DfException {
		session.traceDMCL(i, s);
	}
	
	@Override
	public boolean isServerTraceOptionSet(String s) throws DfException {
		return session.isServerTraceOptionSet(s);
	}
	
	@Override
	public void setServerTraceLevel(int i, String s) throws DfException {
		session.setServerTraceLevel(i, s);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public String apiGet(String s, String s1) throws DfException {
		return session.apiGet(s, s1);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public boolean apiSet(String s, String s1, String s2) throws DfException {
		return session.apiSet(s, s1, s2);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public boolean apiExec(String s, String s1) throws DfException {
		return session.apiExec(s, s1);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public ByteArrayInputStream apiGetBytes(String s, String s1, String s2, String s3, int i) throws DfException {
		return session.apiGetBytes(s, s1, s2, s3, i);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public boolean apiSetBytes(String s, String s1, ByteArrayOutputStream byteArrayOutputStream) throws DfException {
		return session.apiSetBytes(s, s1, byteArrayOutputStream);
	}
	
	/**
	 * @deprecated
	 */
	@Override
	public IDfList apiDesc(String s) throws DfException {
		return session.apiDesc(s);
	}
	
	@Override
	public boolean resetTicketKey() throws DfException {
		return session.resetTicketKey();
	}
	
	@Override
	public String exportTicketKey(String s) throws DfException {
		return session.exportTicketKey(s);
	}
	
	@Override
	public boolean importTicketKey(String s, String s1) throws DfException {
		return session.importTicketKey(s, s1);
	}
	
	@Override
	public void addDynamicGroup(String s) throws DfException {
		session.addDynamicGroup(s);
	}
	
	@Override
	public void removeDynamicGroup(String s) throws DfException {
		session.removeDynamicGroup(s);
	}
	
	@Override
	public int getDynamicGroupCount() throws DfException {
		return session.getDynamicGroupCount();
	}
	
	@Override
	public String getDynamicGroup(int i) throws DfException {
		return session.getDynamicGroup(i);
	}
	
	@Override
	public boolean resetPassword(String s) throws DfException {
		return session.resetPassword(s);
	}
	
	@Override
	public String getApplicationToken(String s, String s1, int i, String s2, boolean b) throws DfException {
		return session.getApplicationToken(s, s1, i, s2, b);
	}
	
	@Override
	public IDfLocalModuleRegistry getModuleRegistry() throws DfException {
		return session.getModuleRegistry();
	}
	
	@Override
	public IDfEnumeration getObjectPaths(IDfId iDfId) throws DfException {
		return session.getObjectPaths(iDfId);
	}
	
	@Override
	public IDfObjectPathsMap getObjectPaths(IDfList iDfList) throws DfException {
		return session.getObjectPaths(iDfList);
	}
	
	@Override
	public void assume(IDfLoginInfo iDfLoginInfo) throws DfException {
		session.assume(iDfLoginInfo);
	}
	
	@Override
	public IDfAcsTransferPreferences getAcsTransferPreferences() {
		return session.getAcsTransferPreferences();
	}
	
	@Override
	public void setAcsTransferPreferences(IDfAcsTransferPreferences iDfAcsTransferPreferences) {
		session.setAcsTransferPreferences(iDfAcsTransferPreferences);
	}
	
	@Override
	public boolean isRestricted() {
		return session.isRestricted();
	}
	
	@Override
	public boolean isDeadlockVictim() {
		return session.isDeadlockVictim();
	}
	
	@Override
	public void publishDataDictionary(String s, String s1, String s2, boolean b) throws DfException {
		session.publishDataDictionary(s, s1, s2, b);
	}
	
	@Override
	public boolean isServerAuthenticated() {
		return session.isServerAuthenticated();
	}
	
	@Override
	public boolean isClientAuthenticated() {
		return session.isClientAuthenticated();
	}
	
	@Override
	public void reparentLightObjects(IDfId iDfId, IDfList iDfList) throws DfException {
		session.reparentLightObjects(iDfId, iDfList);
	}
	
	@Override
	public void flushObject(IDfId iDfId) throws DfException {
		session.flushObject(iDfId);
	}
	
	@Override
	public void flushCachedQuery(String s) throws DfException {
		session.flushCachedQuery(s);
	}
	
	@Override
	public void killSession(IDfId iDfId, String s, String s1) throws DfException {
		session.killSession(iDfId, s, s1);
	}
	
	@Override
	public IDfSessionScopeManager getSessionScopeManager() {
		return session.getSessionScopeManager();
	}
	
	@Override
	public IDfFtConfig getFtConfig() throws DfException {
		return session.getFtConfig();
	}
	
	@Override
	public IDfMessageQueueManager getMessageQueueManager() {
		return session.getMessageQueueManager();
	}
	
	@Override
	public boolean requestDormancy() throws DfException {
		return session.requestDormancy();
	}
	
	@Override
	public boolean requestDormancy(boolean b) throws DfException {
		return session.requestDormancy(b);
	}
	
	@Override
	public DormantStatus checkDormantStatus() throws DfException {
		return session.checkDormantStatus();
	}
	
	@Override
	public DormantStatus checkDormantStatus(boolean b) throws DfException {
		return session.checkDormantStatus(b);
	}
	
	@Override
	public boolean makeActive() throws DfException {
		return session.makeActive();
	}
	
	@Override
	public boolean makeActive(boolean b) throws DfException {
		return session.makeActive(b);
	}
	
	@Override
	public boolean enableSaveInDormantState() throws DfException {
		return session.enableSaveInDormantState();
	}
	
	@Override
	public boolean disableSaveInDormantState() throws DfException {
		return session.disableSaveInDormantState();
	}
	
	@Override
	public boolean projectDormantStatus() throws DfException {
		return session.projectDormantStatus();
	}
	
	@Override
	public boolean projectActiveStatus() throws DfException {
		return session.projectActiveStatus();
	}
	
	@Override
	public IDfCollection startGatheringMetrics(List<String> list) throws DfException {
		return session.startGatheringMetrics(list);
	}
	
	@Override
	public IDfCollection stopGatheringMetrics(List<String> list) throws DfException {
		return session.stopGatheringMetrics(list);
	}
	
	@Override
	public IDfCollection collectMetrics(List<String> list) throws DfException {
		return session.collectMetrics(list);
	}
	
	@Override
	public IDfCollection collectMetrics(List<String> list, String s) throws DfException {
		return session.collectMetrics(list, s);
	}
	
	@Override
	public IDfCollection listMetricsState() throws DfException {
		return session.listMetricsState();
	}
	
	@Override
	public boolean resetMetrics() throws DfException {
		return session.resetMetrics();
	}
	
}
