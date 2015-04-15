package com.docbyte.docshifter.model.vo;

import java.io.Serializable;
import java.util.Set;

public class ChainConfiguration implements Serializable
{
	private static final long serialVersionUID = -4600324113793261377L;

	private int id;
	
	private String name;
	private String description;
	private String printerName;
	private String queueName;
	
	private boolean enabled;

	private Node rootNode;

	public ChainConfiguration() {}

	public ChainConfiguration(String name, String description, boolean enabled, Node rootNode, String printerName, String queueName)
	{
		this.name = name;
		this.description = description;
		this.enabled = enabled;
		this.printerName = printerName;
		this.queueName = queueName;
		this.rootNode = rootNode;
	}
	public String getDescription()
	{
		return description;
	}
	public int getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}

	public Node getRootNode()
	{
		return rootNode;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setRootNode(Node rootNode)
	{
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
}
