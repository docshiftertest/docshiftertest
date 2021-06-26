package com.docshifter.core.task;

import com.docshifter.core.work.WorkFolder;
import java.io.Serializable;

public class VeevaTask extends Task implements Serializable {

	private static final long serialVersionUID = -2850556403171817586L;

	protected String task_id = "";
	protected String items_id;    //unique file identifier: files are inside binders
	protected String binder_id;  //unique binder identifier

	public VeevaTask() {
	}

	public VeevaTask(String binder_id, String item_id, WorkFolder wf) {
		this.task_id = binder_id;
		this.binder_id = binder_id;
		this.items_id = item_id;
		this.workFolder = wf;
	}

	public String getItemsId() {
		return items_id;
	}


	public void setItemsId(String item_id) {
		this.items_id = item_id;
	}

	public String getBinderId() {
		return binder_id;
	}

	public void setBinderId(String binder_id) {
		this.binder_id = binder_id;
	}

	/**
	 * @return the task_id
	 */
	public String getTaskId() {
		return task_id;
	}

	/**
	 * @param task_id the task_id to set
	 */
	public void setTaskId(String task_id) {
		this.task_id = task_id;
	}

	@Override
	public String toString() {
		return "VEEVA Items_ID = " + items_id;
	}
}

