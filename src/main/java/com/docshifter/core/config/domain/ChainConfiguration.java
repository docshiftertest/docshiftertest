package com.docshifter.core.config.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "CHAINCONFIGURATION")
public class ChainConfiguration implements Serializable
{
	private static final long serialVersionUID = -4600324113793261377L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String name;
	private String description;
	private String printerName;
	private String queueName;
	private String errormailHost;
	private String errormailDestination;
	private String errormailOrigin;
	
	private boolean enabled;

	private Node rootNode;

	public ChainConfiguration() {}

	public ChainConfiguration(String name, String description, boolean enabled, Node rootNode, String printerName, String queueName,
							  String errormailOrigin, String errormailDestination, String errormailHost)
	{
		this.name = name;
		this.description = description;
		this.enabled = enabled;
		this.printerName = printerName;
		this.queueName = queueName;
		this.rootNode = rootNode;
		this.errormailDestination = errormailDestination;
		this.errormailHost = errormailHost;
		this.errormailOrigin = errormailOrigin;
	}
	public String getDescription()
	{
		return description;
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public long getId()
	{
		return id;
	}


	public String getName()
	{
		return name;
	}

	@ManyToOne(
			targetEntity=Node.class,
			cascade = CascadeType.ALL,
			fetch = FetchType.EAGER

	)
	@JoinColumn(name="ROOTNODE")
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

	public void setId(long id)
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
