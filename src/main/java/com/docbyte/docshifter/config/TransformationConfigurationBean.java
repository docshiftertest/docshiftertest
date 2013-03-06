package com.docbyte.docshifter.config;

import com.docbyte.docshifter.model.vo.ChainConfiguration;

public class TransformationConfigurationBean {

	private SenderConfigurationBean sender;
	private ReceiverConfigurationBean receiver;
	private long id;
	private String queueName;
	private String printerName;
	private boolean enabled;
	
	public TransformationConfigurationBean() { }

	public TransformationConfigurationBean(ChainConfiguration config){		
		this.sender = new SenderConfigurationBean(config.getSenderConfiguration());
		this.receiver = new ReceiverConfigurationBean(config.getReceiverConfiguration());
		this.id = config.getId();
		this.queueName = config.getQueueName();
		this.printerName = config.getPrinterName();
		this.enabled = config.isEnabled();
	}

	public SenderConfigurationBean getSender() {
		return sender;
	}

	public void setSender(SenderConfigurationBean sender) {
		this.sender = sender;
	}

	public ReceiverConfigurationBean getReceiver() {
		return receiver;
	}

	public void setReceiver(ReceiverConfigurationBean receiver) {
		this.receiver = receiver;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getPrinterName() {
		return printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
