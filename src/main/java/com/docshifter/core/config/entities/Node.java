package com.docshifter.core.config.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
public class Node implements Serializable {
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToMany(cascade = CascadeType.REMOVE)
	@JoinTable(
			name = "node_connection",
			joinColumns = {@JoinColumn(name = "parent_id")},
			inverseJoinColumns = {@JoinColumn(name = "child_id")})
	@JsonIgnore
	@Nonnull
	private Set<Node> parentNodes;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "parentNodes", cascade = CascadeType.ALL)
	@Nonnull
	private Set<Node> childNodes;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	@Nullable
	private ModuleConfiguration moduleConfiguration;

	private Double xPosition;

	private Double yPosition;

	public Node() {
		childNodes = new HashSet<>();
		parentNodes = new HashSet<>();
	}
	
	public Node(@Nonnull Set<Node> parentNodes, @Nullable ModuleConfiguration moduleConfiguration, Double xPosition,
				Double yPosition){
		this();
		setParentNodes(parentNodes);
		this.moduleConfiguration = moduleConfiguration;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
	}

	private Node deepCopy(Map<Node, Node> alreadyEncountered, Set<Node> encounteringChildren, Set<Node> copiedRoots) {
		if (alreadyEncountered.containsKey(this)) {
			return alreadyEncountered.get(this);
		}
		Node copied;
		if (isRoot()) {
			copied = new Node(new HashSet<>(), moduleConfiguration, xPosition, yPosition);
			if (copiedRoots != null) {
				copiedRoots.add(copied);
			}
		} else {
			encounteringChildren.add(this);
			copied = new Node(parentNodes.stream()
					.map(n -> n.deepCopy(alreadyEncountered, encounteringChildren, copiedRoots))
					.collect(Collectors.toUnmodifiableSet()), moduleConfiguration, xPosition, yPosition);
			encounteringChildren.remove(this);
		}
		alreadyEncountered.put(this, copied);
		childNodes.stream()
				.filter(n -> !encounteringChildren.contains(n))
				.forEach(n -> n.deepCopy(alreadyEncountered, encounteringChildren, copiedRoots));
		return copied;
	}

	/**
	 * Performs a deep copy of the entire {@link Node} hierarchy.
	 * @return The copied version of the current node.
	 */
	public Node deepCopy() {
		return deepCopy(new HashMap<>(), new HashSet<>(), null);
	}

	/**
	 * Performs a deep copy of the entire {@link Node} hierarchy.
	 * @return The copied version of all the root nodes in the hierarchy.
	 */
	public Set<Node> deepCopyGetRoots() {
		Set<Node> copiedRoots = new HashSet<>();
		deepCopy(new HashMap<>(), new HashSet<>(), copiedRoots);
		return copiedRoots;
	}

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}


	public Set<Node> getParentNodes() {
		return Collections.unmodifiableSet(parentNodes);
	}

	public void setParentNodes(@Nonnull Set<Node> parentNodes) {
		Objects.requireNonNull(parentNodes);
		clearParents();
		for (Node p : parentNodes) {
			addParent(p);
		}
	}

	public void addChild(@Nonnull Node n) {
		Objects.requireNonNull(n);
		this.childNodes.add(n);
		n.parentNodes.add(this);
	}

	public void removeChild(@Nonnull Node n) {
		Objects.requireNonNull(n);
		this.childNodes.remove(n);
		n.parentNodes.remove(this);
	}

	public void addParent(@Nonnull Node p) {
		Objects.requireNonNull(p);
		this.parentNodes.add(p);
		p.childNodes.add(this);
	}

	public void removeParent(@Nonnull Node p) {
		Objects.requireNonNull(p);
		this.parentNodes.remove(p);
		p.childNodes.remove(this);
	}

	@Nonnull
	public Set<Node> getChildNodes() {
		return Collections.unmodifiableSet(childNodes);
	}

	public void setChildNodes(@Nonnull Set<Node> childNodes) {
		Objects.requireNonNull(childNodes);
		clearChildren();
		for (Node c : childNodes) {
			addChild(c);
		}
	}

	@Nullable
	public ModuleConfiguration getModuleConfiguration(){
		return moduleConfiguration;
	}
	
	public void setModuleConfiguration(@Nullable ModuleConfiguration moduleConfiguration){
		this.moduleConfiguration = moduleConfiguration;
	}

	public Double getXPosition() {
		return xPosition;
	}

	public void setXPosition(Double xPosition) {
		this.xPosition = xPosition;
	}

	public Double getYPosition() {
		return yPosition;
	}

	public void setYPosition(Double yPosition) {
		this.yPosition = yPosition;
	}

	public void clearAllChildNodes() {
		for (Node n : childNodes) {
			n.clearAllChildNodes();
			removeChild(n);
		}
	}
	
	public void clearChildren() {
		for (Node childNode : childNodes) {
			removeChild(childNode);
		}
	}

	public void clearAllParentNodes() {
		for (Node n : parentNodes) {
			n.clearAllParentNodes();
			removeParent(n);
		}
	}

	public void clearParents() {
		for (Node parentNode : parentNodes) {
			removeParent(parentNode);
		}
	}
	
	public boolean compareTo(Object o){
		if(o instanceof Node node){
			if (node.childNodes.size() != childNodes.size()) {
				return false;
			}
			if (node.parentNodes.size() != parentNodes.size()) {
				return false;
			}
			
			for (Node child : childNodes) {
				boolean exists = false;
				for(Node otherChild : node.childNodes) {
					if (child.compareTo(otherChild)) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					return false;
				}
			}

			for (Node parent : parentNodes) {
				boolean exists = false;
				for (Node otherParent : node.parentNodes) {
					if (parent.compareTo(otherParent)) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					return false;
				}
			}
			return true;
			
		}
		return false;
	}

	/**
	 * Performs an action on all nodes in the node hierarchy (starting from this node, then recursively through its
	 * parents, and finally through its children).
	 * @param func The action to perform.
	 */
	public void iterateOverNode(Consumer<? super Node> func){
		iterateOverNode(n -> {
			func.accept(n);
			return true;
		});
	}

	/**
	 * Performs an action on all nodes in the node hierarchy (starting from this node, then recursively through its
	 * parents, and finally through its children). The specified {@link Predicate} may return false to indicate an
	 * early exit.
	 * @param func The action to perform. {@link Predicate} should return {@code true} in order to continue,
	 * {@code false} in order to abort.
	 */
	public void iterateOverNode(Predicate<? super Node> func) {
		iterateOverNode(func, new HashSet<>());
	}

	private boolean iterateOverNode(Predicate<? super Node> func, Set<Node> alreadyEncountered) {
		if (alreadyEncountered.contains(this)) {
			return true;
		}
		alreadyEncountered.add(this);
		if (!func.test(this)) {
			return false;
		}
		for (Node parentNode : parentNodes) {
			if (!parentNode.iterateOverNode(func, alreadyEncountered)) {
				return false;
			}
		}
		for (Node childNode : childNodes) {
			if (!childNode.iterateOverNode(func, alreadyEncountered)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if all the specified nodes are part of the same hierarchy, i.e. if they eventually wire up to each
	 * other through some common parent or child.
	 * @param nodesToCheck The nodes to check.
	 * @return {@code true} if all of the specified nodes are part of the same hierarchy, {@code false} otherwise.
	 */
	public static boolean areAllNodesConnected(Set<Node> nodesToCheck) {
		return areAllNodesConnected(nodesToCheck.toArray(new Node[0]));
	}

	/**
	 * Checks if all the specified nodes are part of the same hierarchy, i.e. if they eventually wire up to each
	 * other through some common parent or child.
	 * @param nodesToCheck The nodes to check.
	 * @return {@code true} if all of the specified nodes are part of the same hierarchy, {@code false} otherwise.
	 */
	public static boolean areAllNodesConnected(Node... nodesToCheck) {
		if (nodesToCheck.length <= 1) {
			return true;
		}

		Set<Node> checkSet = Arrays.stream(nodesToCheck).skip(1).collect(Collectors.toSet());
		nodesToCheck[0].iterateOverNode(node -> {
			if (checkSet.remove(node)) {
				return !checkSet.isEmpty();
			}
			return true;
		});
		return checkSet.isEmpty();
	}

	@Transient
	public int getTotalChildNodesCount() {
		if(isLeaf()) {
			return 0;
		} else {
			int i = 0;
			for (Node n : childNodes) {
				i += n.getTotalChildNodesCount();
			}
			return i;
		}
	}

	@Transient
	public boolean isRoot() {
		return parentNodes.isEmpty();
	}

	@Transient
	@JsonIgnore
	public Set<Node> getRoots() {
		if (isRoot()) {
			return Set.of(this);
		}

		return parentNodes.stream()
				.map(Node::getRoots)
				.flatMap(Set::stream)
				.collect(Collectors.toUnmodifiableSet());
	}

	@Transient
	public boolean isLeaf(){
		return childNodes.isEmpty();
	}
}
