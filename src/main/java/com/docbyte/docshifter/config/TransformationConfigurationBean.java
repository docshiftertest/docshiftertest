package com.docbyte.docshifter.config;

import java.util.Set;

import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.ReceiverConfiguration;

public class TransformationConfigurationBean {

	private SenderConfigurationBean sender;
	private Set<ReceiverConfigurationBean> receivers;
	private long id;
	private String queueName;
	private String printerName;
	private boolean enabled;
	
	public TransformationConfigurationBean() { }

	public TransformationConfigurationBean(ChainConfiguration config){		
			this.sender = new SenderConfigurationBean(config.getSenderConfiguration());
			for(ReceiverConfiguration receiverconfig:config.getReceiverConfiguration())
				receivers.add(new ReceiverConfigurationBean(receiverconfig));
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

	public Set<ReceiverConfigurationBean> getReceivers() {
		return receivers;
	}

	public void setReceivers(Set<ReceiverConfigurationBean> receivers) {
		this.receivers = receivers;
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
