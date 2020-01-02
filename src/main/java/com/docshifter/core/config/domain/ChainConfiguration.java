package com.docshifter.core.config.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ChainConfiguration {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	private String name;
	private String description;
	private String printerName;
	private String queueName;

	private long timeout;
	@Column(columnDefinition = "int default 2")
	private  Integer priority;

	private boolean enabled;

	@ManyToOne(cascade=CascadeType.ALL)
	private Node rootNode;


	public ChainConfiguration() {}

	public ChainConfiguration(String name, String description, boolean enabled, Node rootNode, String printerName, String queueName,
			  long timeout, long priority) {
		this(name, description, enabled, rootNode, printerName, queueName, timeout, Integer.valueOf((int) priority));
	}

	public ChainConfiguration(String name, String description, boolean enabled, Node rootNode, String printerName, String queueName,
							  long timeout, int priority) {
		this.name = name;
		this.description = description;
		this.enabled = enabled;
		this.printerName = printerName;
		this.queueName = queueName;
		this.rootNode = rootNode;
		this.timeout = timeout;
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}


	public long getId() {
		return id;
	}


	public String getName()
	{
		return name;
	}


	public Node getRootNode() {
		return rootNode;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setId(long id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}

	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}

	public String getPrinterName() {
		return printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public void setPriority(long priority) {
		this.setPriority(Integer.valueOf((int) priority));
	}

	@Override
	public String toString() {
		StringBuilder sBuf = new StringBuilder();
		sBuf.append("ChainConfiguration = {");
		sBuf.append("Name: ");
		sBuf.append(this.name);
		sBuf.append(", Description: ");
		sBuf.append(this.description);
		sBuf.append(", Enabled: ");
		sBuf.append(this.enabled);
		sBuf.append(", Printer Name: ");
		sBuf.append(this.printerName);
		sBuf.append(", Queue Name: ");
		sBuf.append(this.queueName);
		sBuf.append(", Root Node: ");
		sBuf.append(this.rootNode);
		sBuf.append(", Timeout: ");
		sBuf.append(this.timeout);
		sBuf.append(", Priority: ");
		sBuf.append(this.priority);
		sBuf.append("}");
		return sBuf.toString();
	}
}
