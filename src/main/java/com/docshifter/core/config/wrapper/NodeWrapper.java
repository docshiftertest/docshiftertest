package com.docshifter.core.config.wrapper;

import com.docshifter.core.config.domain.ChainConfiguration;
import com.docshifter.core.config.domain.ChainConfigurationRepository;
import com.docshifter.core.config.domain.Node;

public abstract class NodeWrapper {

	private Node node;
	private ChainConfiguration chainConfiguration;
	
	private long configurationId;
	private String queueName;
	private String printerName;
	private boolean configurationEnabled;

	private String errormailHost;
	private String errormailDestination;
	private String errormailOrigin;
	
	private ModuleWrapper moduleWrapper;

	protected ChainConfigurationRepository chainConfigurationRepository;
	
	public NodeWrapper(Node n, ChainConfigurationRepository chainConfigurationRepository){
		this.node = n;
		this.chainConfigurationRepository = chainConfigurationRepository;

		Node rootNode = n;
		if(n!=null)
		{
			while(rootNode.getParentNode() != null)
				rootNode = rootNode.getParentNode();
		}
		chainConfiguration = chainConfigurationRepository.findByRootNode(rootNode);
		
		this.configurationId = chainConfiguration.getId();
		this.queueName = chainConfiguration.getQueueName();
		this.printerName = chainConfiguration.getPrinterName();
		this.configurationEnabled = chainConfiguration.isEnabled();
		this.errormailDestination = chainConfiguration.getErrormailDestination();
		this.errormailHost = chainConfiguration.getErrormailHost();
		this.errormailOrigin = chainConfiguration.getErrormailOrigin();

		this.moduleWrapper = new ModuleWrapper(n.getModuleConfiguration());
	}
	
	public NodeWrapper() {}

	public Node getNode(){
		return node;
	}
	
	protected ChainConfiguration getChainConfiguration(){
		return chainConfiguration;
	}
	
	public long getID() {
		return configurationId;
	}

	public void setId(long id) {
		this.configurationId = id;
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
		return configurationEnabled;
	}

	public void setEnabled(boolean enabled) {
		this.configurationEnabled = enabled;
	}
	
	public ModuleWrapper getModuleWrapper(){
		return this.moduleWrapper;
	}

	public String getErrormailHost() {
		return errormailHost;
	}

	public void setErrormailHost(String errormailHost) {
		this.errormailHost = errormailHost;
	}

	public String getErrormailDestination() {
		return errormailDestination;
	}

	public void setErrormailDestination(String errormailDestination) {
		this.errormailDestination = errormailDestination;
	}

	public String getErrormailOrigin() {
		return errormailOrigin;
	}

	public void setErrormailOrigin(String errormailOrigin) {
		this.errormailOrigin = errormailOrigin;
	}
}
