package com.docshifter.core.config.entities;

import com.docshifter.core.operations.FailureLevel;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.TypeName;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;


//Read-only: Use this strategy when you are sure that your data never changes. If you try to update the data with this strategy Hibernate will throw an exception.
//Read-write: Use this strategy when you do lots of updates on your entity. It guarantees data consistency when multiple transactions try to access the same object. The transaction which accesses the object first acquires the lock and other transactions will not have access to the cache and will start fetching the data directly from the database.
//Nonstrict-read-write:  It is similar to Read-write but there is no locking mechanism hence it does not guarantee data consistency between cache and database. Use this strategy when stale data for a small window is not a concern.
//Transactional: It is suitable in a JTA environment. Any changes in the cached entity will be committed or rollback in the same transaction.
//Using read-write strategy as we will can have a lot of changes during workflows configuration.
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
@TypeName("ChainConfiguration")
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
	private Integer priority;

	private boolean enabled;

	@Enumerated(EnumType.STRING)
	private FailureLevel failureLevel;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne(cascade=CascadeType.ALL)
	private Node rootNode;

	public ChainConfiguration() {}

	public ChainConfiguration(String name, String description, boolean enabled, Node rootNode, String printerName, String queueName,
			  long timeout, long priority, FailureLevel failureLevel) {
		this(name, description, enabled, rootNode, printerName, queueName, timeout, (int) priority, failureLevel);
	}

	public ChainConfiguration(String name, String description, boolean enabled, Node rootNode, String printerName, String queueName,
							  long timeout, int priority, FailureLevel failureLevel) {
		this.name = name;
		this.description = description;
		this.enabled = enabled;
		this.printerName = printerName;
		this.queueName = queueName;
		this.rootNode = rootNode;
		this.timeout = timeout;
		this.priority = priority;
		this.failureLevel = failureLevel;
	}

	public String getDescription() {
		return description == null ? "" : description;
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
		return printerName == null ? "" : printerName;
	}

	public void setPrinterName(String printerName) {
		this.printerName = printerName;
	}

	public String getQueueName() {
		return queueName == null ? "" : queueName;
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

	public FailureLevel getFailureLevel() {
		return failureLevel;
	}

	public void setFailureLevel(FailureLevel failureLevel) {
		this.failureLevel = failureLevel;
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
		sBuf.append(", FailureLevel: ");
		sBuf.append(this.failureLevel);
		sBuf.append("}");
		return sBuf.toString();
	}
}
