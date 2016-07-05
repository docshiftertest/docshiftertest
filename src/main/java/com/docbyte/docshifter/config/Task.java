package com.docbyte.docshifter.config;

import com.docbyte.docshifter.util.Logger;
import com.docbyte.docshifter.work.WorkFolder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
 * Value object which represents a dmi_queue_item object.
 * 
 * @author $Author$
 * @version $Rev$
 * Last Modification Date: $Date$
 *
 */
public class Task implements Serializable {

	private static final long serialVersionUID = 4087826709318179760L;
	private Byte[] content;
	protected String name="";		// filepath for dctm and filesystem tasks  (or filename if content is used)
	protected String task_id="";
	protected String event="";	//type of transformation request: default from dctm = "rendition"
	protected String item_id="";	//unique file identifier: default from dctm= objectid
	protected String request="";	//transformation request: default from dctm= "rendition_req_ps_pdf"
	protected String sent_by="";	//user requesting the transformation: default from dctm= "dm_autorender_win31"
	protected WorkFolder workFolder=null;
	protected HashMap<String, String> parameters;
	protected Map<String, Object> data;

	//For the Russia Time Stamping
/*	public Task() {

		//08 June 2016
		final long end = 1465992000;
		final long now = System.currentTimeMillis()/1000L;

		//01 March 2016
		final long start = 1456833600;

		Logger.info("THIS TRIALVERSION WILL END ON 01/06/2016", null);
		if (now-start < 0) {
			Logger.info("TRIAL EXPIRED", null);
			System.exit(9);
		}

		if (now > end) {
			Logger.info("TRIAL EXPIRED", null);
			System.exit(9);
		}
	}
*/
	public Task(){}
	/**
	 * @return the content
	 */
	public Byte[] getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(Byte[] content) {
		this.content = content;
	}
	public Task(String filePath, WorkFolder wf){
		//this();
		this.item_id=filePath;
		this.name=filePath;
		this.workFolder = wf;
	}
	
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getSent_by() {
		return sent_by;
	}
	public void setSent_by(String sent_by) {
		this.sent_by = sent_by;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public String getMessage() {
		return request;
	}
	public void setMessage(String message) {
		this.request = message;
	}
	public String getFilePath(){
		return this.name;
	}
	public void setFilePath(String filePath){
		this.name=filePath;
	}
	public String getItem_id() {
		return item_id;
	}
	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public WorkFolder getWorkFolder() {
		return workFolder;
	}
	public void setWorkFolder(WorkFolder workFolder) {
		this.workFolder = workFolder;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}
	//temp to show sending a task through jms works (ObjectMessage)
	public String toString(){
		return item_id+" "+event+" "+request;
	}
	/**
	 * @return the task_id
	 */
	public String getTask_id() {
		return task_id;
	}
	/**
	 * @param task_id the task_id to set
	 */
	public void setTask_id(String task_id) {
		this.task_id = task_id;
	}
	
	
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> map) {
		this.data = map;
	}
}
