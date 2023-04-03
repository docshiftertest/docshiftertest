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

/**
 * If an apple is the ingredient to make apple cake, then a node is an ingredient to make a workflow
 * (or {@link ChainConfiguration} to be more correct)! Each node has a {@link ModuleConfiguration} and can be chained
 * to one or more other nodes. That way, we can build an entire node hierarchy of parents and children, from root to
 * leaf.
 */
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
			joinColumns = {@JoinColumn(name = "child_id")},
			inverseJoinColumns = {@JoinColumn(name = "parent_id")})
	@Nonnull
	@JsonIgnore
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

	/**
	 * Recursive method that performs a deep copy of the entire {@link Node} hierarchy.
	 * @param alreadyEncountered Keeps track of {@link Node}s that have already been encountered in the hierarchy
	 *                              (= keys), and their copied versions (= values) that can be returned if we've
	 *                              already encountered the current {@link Node}.
	 * @param encounteringChildren Keeps track of child {@link Node}s that we are currently copying. This is to
	 *                                prevent us from getting stuck in an infinite loop and performing double work
	 *                                when we visit a parent node and then their children and then another parent
	 *                                node that has common children and such...
	 * @param copiedRoots If not {@code null}, all of the deeply copied root {@link Node}s will be stored in this
	 * {@link Set}.
	 * @return The copied version of the current node.
	 */
	private Node deepCopy(Map<Node, Node> alreadyEncountered, Set<Node> encounteringChildren, Set<Node> copiedRoots) {
		if (alreadyEncountered.containsKey(this)) {
			return alreadyEncountered.get(this);
		}
		Node copied;
		if (isRoot()) {
			// Root nodes have no parents, so use that specific constructor overload
			// Just need to copy this node
			copied = new Node(new HashSet<>(), moduleConfiguration, xPosition, yPosition);
			// Make sure to track our copied root if it's necessary
			if (copiedRoots != null) {
				copiedRoots.add(copied);
			}
		} else {
			// We're currently copying this node, so add it to the active encounteringChildren set
			encounteringChildren.add(this);
			// Make sure to recurse and copy the entire parent hierarchy first, before copying this node
			copied = new Node(parentNodes.stream()
					.map(n -> n.deepCopy(alreadyEncountered, encounteringChildren, copiedRoots))
					.collect(Collectors.toUnmodifiableSet()), moduleConfiguration, xPosition, yPosition);
			// Done copying this node, so we can remove it
			encounteringChildren.remove(this);
		}
		alreadyEncountered.put(this, copied);
		// Finally check if there are any children left that need copying (so nodes that we're currently not
		// evaluating).
		childNodes.stream()
				.filter(n -> !encounteringChildren.contains(n))
				.forEach(n -> n.deepCopy(alreadyEncountered, encounteringChildren, copiedRoots));
		return copied;
	}

	/**
	 * Performs a deep copy of the entire {@link Node} hierarchy.
	 * @return The copied version of the current node.
	 */
	@Transient
	@JsonIgnore
	public Node deepCopy() {
		return deepCopy(new HashMap<>(), new HashSet<>(), null);
	}

	/**
	 * Performs a deep copy of the entire {@link Node} hierarchy.
	 * @return The copied version of all the root nodes in the hierarchy (and of course you can walk through this
	 * entire hierarchy from these root nodes).
	 */
	@Transient
	@JsonIgnore
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

	/**
	 * Overwrites the parent nodes with a different {@link Set} of parent nodes.
	 */
	public void setParentNodes(@Nonnull Set<Node> parentNodes) {
		Objects.requireNonNull(parentNodes);
		// Clear the (direct) parents of this node first to make sure we don't get any broken references (i.e. this
		// node no longer pointing to a parent but said parent still pointing to this child)
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

	/**
	 * Overwrites the child nodes with a different {@link Set} of child nodes.
	 */
	public void setChildNodes(@Nonnull Set<Node> childNodes) {
		Objects.requireNonNull(childNodes);
		// Clear the (direct) children of this node first to make sure we don't get any broken references (i.e. this
		// node no longer pointing to a child but said child still pointing to this parent)
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

	/**
	 * Since version 8.0, we need to store the visual positions of each node, so they can be rendered correctly in the
	 * brand-new workflow editor... So this delivers you the X coordinate.
	 */
	public Double getXPosition() {
		return xPosition;
	}

	/**
	 * Since version 8.0, we need to store the visual positions of each node, so they can be rendered correctly in the
	 * brand-new workflow editor... So this allows you to set the X coordinate.
	 */
	public void setXPosition(Double xPosition) {
		this.xPosition = xPosition;
	}

	/**
	 * Since version 8.0, we need to store the visual positions of each node, so they can be rendered correctly in the
	 * brand-new workflow editor... So this delivers you the Y coordinate.
	 */
	public Double getYPosition() {
		return yPosition;
	}

	/**
	 * Since version 8.0, we need to store the visual positions of each node, so they can be rendered correctly in the
	 * brand-new workflow editor... So this allows you to set the Y coordinate.
	 */
	public void setYPosition(Double yPosition) {
		this.yPosition = yPosition;
	}

	/**
	 * Detaches the ENTIRE child hierarchy from the current node.
	 */
	public void clearAllChildNodes() {
		for (Node n : childNodes) {
			n.clearAllChildNodes();
			removeChild(n);
		}
	}

	/**
	 * Clears all the child nodes from the current node.
	 */
	public void clearChildren() {
		for (Node childNode : childNodes) {
			removeChild(childNode);
		}
	}

	/**
	 * Detaches the ENTIRE parent hierarchy from the current node.
	 */
	public void clearAllParentNodes() {
		for (Node n : parentNodes) {
			n.clearAllParentNodes();
			removeParent(n);
		}
	}

	/**
	 * Clears all the parent nodes from the current node.
	 */
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
				boolean childExists = false;
				for(Node otherChild : node.childNodes) {
					if (child.compareTo(otherChild)) {
						childExists = true;
						break;
					}
				}
				if (!childExists) {
					return false;
				}
			}

			for (Node parent : parentNodes) {
				boolean parentExists = false;
				for (Node otherParent : node.parentNodes) {
					if (parent.compareTo(otherParent)) {
						parentExists = true;
						break;
					}
				}
				if (!parentExists) {
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
	 * parents, and finally through its children). The specified {@link Predicate} may return {@code false} to
	 * indicate an early exit.
	 * @param func The action to perform. {@link Predicate} should return {@code true} in order to continue,
	 * {@code false} in order to abort.
	 */
	public void iterateOverNode(Predicate<? super Node> func) {
		iterateOverNode(func, new HashSet<>());
	}

	/**
	 * Recursive method that performs an action on all nodes in the node hierarchy (starting from this node, then
	 * recursively through its parents, and finally through its children). The specified {@link Predicate} may return
	 * {@code false} to indicate an early exit.
	 * @param func The action to perform. {@link Predicate} should return {@code true} in order to continue,
	 * {@code false} in order to abort.
	 * @param alreadyEncountered Keeps track of {@link Node}s that have already been encountered in the hierarchy so
	 *                              we can skip over if we encounter them again.
	 * @return {@code false} if an abort was signalled, {@code true} otherwise.
	 */
	private boolean iterateOverNode(Predicate<? super Node> func, Set<Node> alreadyEncountered) {
		if (!alreadyEncountered.add(this)) {
			// If we already encountered this node, carry on...
			return true;
		}
		// Run the predicate for the current node
		if (!func.test(this)) {
			return false;
		}
		// Then recurse over all the parent nodes
		for (Node parentNode : parentNodes) {
			if (!parentNode.iterateOverNode(func, alreadyEncountered)) {
				return false;
			}
		}
		// And finally the child nodes
		for (Node childNode : childNodes) {
			if (!childNode.iterateOverNode(func, alreadyEncountered)) {
				return false;
			}
		}
		// If we get to this point, carry on (otherwise we're in abort mode)
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

		// We will iterate starting from the first node in the array, the other nodes can be stored in our "check set".
		// Basically we need to find each node in our check set while iterating over the node structure (originating
		// from the first node), only then can we be certain that all the provided nodes are in some way connected
		// together.
		Set<Node> checkSet = Arrays.stream(nodesToCheck)
				.skip(1)
				.collect(Collectors.toSet());
		nodesToCheck[0].iterateOverNode(node -> {
			if (checkSet.remove(node)) {
				return !checkSet.isEmpty();
			}
			return true;
		});
		return checkSet.isEmpty();
	}

	@Transient
	@JsonIgnore
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
	@JsonIgnore
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
	@JsonIgnore
	public boolean isLeaf(){
		return childNodes.isEmpty();
	}
}
