package com.docshifter.core.task;

import com.docshifter.core.work.WorkFolder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * Created by michiel.vandriessche@docbyte.com on 9/6/16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public class DctmTask extends Task implements Serializable {



	protected String event="";	//type of transformation request: default from dctm = "rendition"
	protected String request="";	//transformation request: default from dctm= "rendition_req_ps_pdf"
	protected String sent_by="";	//user requesting the transformation: default from dctm= "dm_autorender_win31"
	protected String task_id="";
	protected String item_id;    //unique file identifier: default from dctm= objectid

	public DctmTask() {}

	public DctmTask(String item_id, WorkFolder wf) {
		this.item_id = item_id;
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

	public String getItem_id() {
		return item_id;
	}

	public void setItem_id(String item_id) {
		this.item_id = item_id;
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

	@Override
	public String toString() {
		return "DCTM Item_ID = " + item_id;
	}
}

