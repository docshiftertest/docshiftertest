package com.docshifter.core.config.entities;

import com.docshifter.core.operations.FailureLevel;
import org.apache.commons.lang3.StringUtils;
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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * An {@link Entity} that represents a workflow in DocShifter. A workflow has a couple of core configuration properties
 * (such as a title, description, timeout,...) as well as an entire {@link Node} hierarchy (characterized by a
 * {@link Set} of {@link #rootNodes root nodes}) and a linked
 * {@link com.docshifter.core.monitoring.entities.Configuration monitoring configuration}.
 */
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
	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(
			name = "chain_configuration_root_node",
			joinColumns = {@JoinColumn(name = "chain_configuration_id")},
			inverseJoinColumns = {@JoinColumn(name = "node_id")})
	@DiffIgnore
	@Nonnull
	private Set<Node> rootNodes;

	@DiffInclude
	private UUID uuid;

	@DiffIgnore
	private LocalDateTime lastModifiedDate;

	// This is a String because this class is a database entity and to persist a List/Set to the database you need
	// another table to hold all your references in. So in this case we're working around that requirement by storing
	// it as a simple text record internally, where the different (enum) elements are split by comma. NOTE: the
	// public-facing getters/setters for this field do work with an EnumSet.
	@DiffIgnore
	private String brokenRules;

	@DiffInclude
	private boolean dsexpressEnabled;

	/**
	 * Default constructor for JPA.
	 */
	public ChainConfiguration() {
		rootNodes = new HashSet<>();
	}

	/**
	 * Constructor that takes in all the parameters of a {@link ChainConfiguration}, but the {@code priority} as a
	 * {@code long}.
	 */
	public ChainConfiguration(String name, String description, boolean enabled, @Nonnull Set<Node> rootNodes,
							  String printerName, String queueName, long timeout, long priority, FailureLevel failureLevel,
							  LocalDateTime lastModifiedDate, @Nonnull Set<WorkflowRule> brokenRules, UUID uuid, boolean dsexpressEnabled) {
		this(name, description, enabled, rootNodes, printerName, queueName, timeout, (int) priority, failureLevel,
				lastModifiedDate, brokenRules, uuid, dsexpressEnabled);
	}

	/**
	 * Constructor that deeply copies another {@link ChainConfiguration}. NOTE: the ID and UUID will not be copied
	 * over, so make sure to set it manually after constructing, if needed!
	 */
	public ChainConfiguration(ChainConfiguration copyMe) {
		this(copyMe.name, copyMe.description, copyMe.enabled,
				copyMe.rootNodes.stream()
						.findAny()
						.map(Node::deepCopyGetRoots)
						.orElse(new HashSet<>()), copyMe.printerName, copyMe.queueName, copyMe.timeout,
				copyMe.priority, copyMe.failureLevel, copyMe.lastModifiedDate, copyMe.getBrokenRules(),
				copyMe.isDsexpressEnabled());
	}

	/**
	 * Constructor that takes in all the parameters of a {@link ChainConfiguration}, except a {@code uuid}, which
	 * will be randomly generated.
	 */
	public ChainConfiguration(String name, String description, boolean enabled, @Nonnull Set<Node> rootNodes,
							  String printerName, String queueName, long timeout, int priority,
							  FailureLevel failureLevel, LocalDateTime lastModifiedDate,
							  @Nonnull Set<WorkflowRule> brokenRules,
							  boolean dsexpressEnabled) {
		this(name, description, enabled, rootNodes, printerName, queueName, timeout, priority, failureLevel,
				lastModifiedDate, brokenRules, UUID.randomUUID(), dsexpressEnabled);
	}

	/**
	 * Constructor that takes in all the parameters of a {@link ChainConfiguration}.
	 */
	public ChainConfiguration(String name, String description, boolean enabled, @Nonnull Set<Node> rootNodes,
							  String printerName, String queueName, long timeout, int priority, FailureLevel failureLevel,
							  LocalDateTime lastModifiedDate, @Nonnull Set<WorkflowRule> brokenRules, UUID uuid,
							  boolean dsexpressEnabled) {
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
		this.lastModifiedDate = lastModifiedDate;
		setBrokenRules(brokenRules);
		this.dsexpressEnabled = dsexpressEnabled;
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

	public boolean isDsexpressEnabled() {
		return dsexpressEnabled;
	}

	public void setDsexpressEnabled(boolean postShifterEnabled) {
		this.dsexpressEnabled = postShifterEnabled;
	}

	/**
	 * Performs an action on all the nodes for this workflow.
	 * @param action The action to perform.
	 */
	public void forEachNode(Consumer<? super Node> action) {
		rootNodes.stream()
				.findAny()
				.ifPresent(n -> n.iterateOverNode(action));
	}

	public LocalDateTime getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	/**
	 * Gets the requirements (i.e. {@link WorkflowRule}s) that the current workflow has not met for it to be
	 * considered complete. If the returned {@link Set} is empty, then the current workflow is in a complete state of
	 * course.
	 */
	@Nonnull
	public Set<WorkflowRule> getBrokenRules() {
		if (StringUtils.isEmpty(brokenRules)) {
			return EnumSet.noneOf(WorkflowRule.class);
		}
		return Collections.unmodifiableSet(
				Arrays.stream(brokenRules.split(","))
						.map(WorkflowRule::valueOf)
						.collect(Collectors.toCollection(() -> EnumSet.noneOf(WorkflowRule.class)))
		);
	}

	/**
	 * Sets the requirements (i.e. {@link WorkflowRule}s) that the current workflow has not met for it to be
	 * considered complete.
	 */
	public void setBrokenRules(@Nonnull Set<WorkflowRule> brokenRules) {
		Objects.requireNonNull(brokenRules);
		this.brokenRules = brokenRules.stream()
				.map(WorkflowRule::name)
				.collect(Collectors.joining(","));
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
				", Last Modified Date: " +
				this.lastModifiedDate +
				", Broken Rules: " +
				this.brokenRules +
				", DSExpress Enabled: " +
				this.dsexpressEnabled +
				"}";
	}
}
