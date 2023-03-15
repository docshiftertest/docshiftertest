package com.docshifter.core.config.entities;

import com.docshifter.core.operations.FailureLevel;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.DiffInclude;

import javax.annotation.Nonnull;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;


//Read-only: Use this strategy when you are sure that your data never changes. If you try to update the data with this strategy Hibernate will throw an exception.
//Read-write: Use this strategy when you do lots of updates on your entity. It guarantees data consistency when multiple transactions try to access the same object. The transaction which accesses the object first acquires the lock and other transactions will not have access to the cache and will start fetching the data directly from the database.
//Nonstrict-read-write:  It is similar to Read-write but there is no locking mechanism hence it does not guarantee data consistency between cache and database. Use this strategy when stale data for a small window is not a concern.
//Transactional: It is suitable in a JTA environment. Any changes in the cached entity will be committed or rollback in the same transaction.
//Using read-write strategy as we will can have a lot of changes during workflows configuration.
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
public class ChainConfiguration implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;

	@DiffInclude
	private String name;

	@DiffInclude
	private String description;

	@DiffInclude
	private String printerName;

	@DiffInclude
	private String queueName;

	@DiffInclude
	private long timeout;

	@DiffInclude
	@Column(columnDefinition = "int default 5")
	private Integer priority;

	@DiffInclude
	private boolean enabled;

	@DiffInclude
	@Enumerated(EnumType.STRING)
	private FailureLevel failureLevel;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OneToMany(cascade=CascadeType.ALL)
	@JoinTable(
			name = "chain_configuration_root_node",
			joinColumns = {@JoinColumn(name = "chain_configuration_id")},
			inverseJoinColumns = {@JoinColumn(name = "node_id")})
	@DiffIgnore
	@Nonnull
	private Set<Node> rootNodes;

	@DiffInclude
	private UUID uuid;

	public ChainConfiguration() {
		rootNodes = new HashSet<>();
	}

	public ChainConfiguration(String name, String description, boolean enabled, @Nonnull Set<Node> rootNodes,
							  String printerName, String queueName, long timeout, long priority, FailureLevel failureLevel,
							  UUID uuid) {
		this(name, description, enabled, rootNodes, printerName, queueName, timeout, (int) priority, failureLevel, uuid);
	}

	public ChainConfiguration(ChainConfiguration copyMe) {
		this(copyMe.name, copyMe.description, copyMe.enabled,
				copyMe.rootNodes.stream()
						.findAny()
						.map(Node::deepCopyGetRoots)
						.orElse(new HashSet<>()), copyMe.printerName, copyMe.queueName, copyMe.timeout,
				copyMe.priority, copyMe.failureLevel);
	}

	public ChainConfiguration(String name, String description, boolean enabled, @Nonnull Set<Node> rootNodes,
							  String printerName, String queueName, long timeout, int priority,
							  FailureLevel failureLevel) {
		this(name, description, enabled, rootNodes, printerName, queueName, timeout, priority, failureLevel,
				UUID.randomUUID());
	}

	public ChainConfiguration(String name, String description, boolean enabled, @Nonnull Set<Node> rootNodes,
							  String printerName, String queueName, long timeout, int priority, FailureLevel failureLevel,
							  UUID uuid) {
		this.name = name;
		this.description = description;
		this.enabled = enabled;
		this.printerName = printerName;
		this.queueName = queueName;
		setRootNodes(rootNodes);
		this.timeout = timeout;
		this.priority = priority;
		this.failureLevel = failureLevel;
		this.uuid = uuid;
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


	@Nonnull
	public Set<Node> getRootNodes() {
		return rootNodes;
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

	public void setRootNodes(@Nonnull Set<Node> rootNodes) {
		Objects.requireNonNull(rootNodes);
		this.rootNodes = rootNodes;
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

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * Performs an action on all the nodes for this workflow.
	 * @param action The action to perform.
	 */
	public void forEachNode(Consumer<? super Node> action) {
		rootNodes.stream()
				.findAny()
				.orElseThrow()
				.iterateOverNode(action);
	}

	@Override
	public String toString() {
		return "ChainConfiguration = {" +
				"Name: " +
				this.name +
				", Description: " +
				this.description +
				", Enabled: " +
				this.enabled +
				", Printer Name: " +
				this.printerName +
				", Queue Name: " +
				this.queueName +
				", Root Nodes: " +
				this.rootNodes +
				", Timeout: " +
				this.timeout +
				", Priority: " +
				this.priority +
				", FailureLevel: " +
				this.failureLevel +
				", uuid: " +
				this.uuid +
				"}";
	}
}
