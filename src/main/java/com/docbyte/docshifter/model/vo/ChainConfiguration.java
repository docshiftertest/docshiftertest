package com.docbyte.docshifter.model.vo;

import java.io.Serializable;

public class ChainConfiguration implements Serializable
{
	private static final long serialVersionUID = -4600324113793261377L;

	private long id;
	
	private String name;
	private String description;
	private String printerName;
	private String queueName;
	
	private boolean enabled;

	private SenderConfiguration senderConfiguration;
	private ReceiverConfiguration receiverConfiguration;

	public ChainConfiguration() {}

	public ChainConfiguration(String name, String description, boolean enabled, SenderConfiguration senderConfiguration, ReceiverConfiguration receiverConfiguration, String printerName, String queueName)
	{
		this.name = name;
		this.description = description;
		this.enabled = enabled;
		this.senderConfiguration = senderConfiguration;
		this.receiverConfiguration = receiverConfiguration;
		this.printerName = printerName;
		this.queueName = queueName;
	}
	public String getDescription()
	{
		return description;
	}
	public long getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}

	public ReceiverConfiguration getReceiverConfiguration()
	{
		return receiverConfiguration;
	}

	public SenderConfiguration getSenderConfiguration()
	{
		return senderConfiguration;
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
	
	public void setId(long id)
	{
		this.id = id;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setReceiverConfiguration(ReceiverConfiguration receiverConfiguration)
	{
		this.receiverConfiguration = receiverConfiguration;
	}
	
	public void setSenderConfiguration(SenderConfiguration senderConfiguration)
	{
		this.senderConfiguration = senderConfiguration;
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
