package com.docshifter.core.config.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
	private Set<Node> parentNodes;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "parentNodes", cascade = CascadeType.ALL)
	private Set<Node> childNodes;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	private ModuleConfiguration moduleConfiguration;

	public Node(){}
	
	public Node(Set<Node> parentNodes, ModuleConfiguration moduleConfiguration){
		setParentNodes(parentNodes);
		if (parentNodes != null) {
			for (Node parentNode : parentNodes) {
				parentNode.addChild(this);
			}
		} else {
			this.parentNodes = new HashSet<>();
		}
		if (childNodes == null) {
			this.childNodes = new HashSet<>();
		}
		this.moduleConfiguration = moduleConfiguration;
	}

	private Node deepCopy(Map<Node, Node> alreadyEncountered, Set<Node> encounteringChildren) {
		if (alreadyEncountered.containsKey(this)) {
			return alreadyEncountered.get(this);
		}
		Node copied;
		if (isRoot()) {
			copied = new Node(null, moduleConfiguration);
		} else {
			encounteringChildren.add(this);
			copied = new Node(parentNodes.stream()
					.map(node -> deepCopy(alreadyEncountered, encounteringChildren))
					.collect(Collectors.toUnmodifiableSet()), moduleConfiguration);
			encounteringChildren.remove(this);
		}
		alreadyEncountered.put(this, copied);
		childNodes.stream()
				.filter(n -> !encounteringChildren.contains(n))
				.forEach(n -> n.deepCopy(alreadyEncountered, encounteringChildren));
		return copied;
	}

	public Node deepCopy() {
		return deepCopy(new HashMap<>(), new HashSet<>());
	}

	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}


	public Set<Node> getParentNodes(){
		return parentNodes;
	}
	
	public void setParentNodes(Set<Node> parentNodes){
		this.parentNodes = parentNodes;
	}
	public void addChild(Node n){
		this.childNodes.add(n);
	}

	public Set<Node> getChildNodes(){
		return childNodes;
	}

	public void setChildNodes(Set<Node> childNodes){
		if (childNodes == null) {
			childNodes = new HashSet<>();
		}
		this.childNodes = childNodes;
	}

	public ModuleConfiguration getModuleConfiguration(){
		return moduleConfiguration;
	}
	
	public void setModuleConfiguration(ModuleConfiguration moduleConfiguration){
		this.moduleConfiguration = moduleConfiguration;
	}

	public void clearAllChildNodes(){
		for(Node n : childNodes){
			n.clearAllChildNodes();
		}
		childNodes.clear();
	}
	
	public void clearChild(){
		this.childNodes.clear();
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
	
	public void iterateOverNode(Consumer<Node> func){
		func.accept(this);
		if(childNodes.size() > 0){
			for(Node n : childNodes)
				func.accept(n);
		}
	}

	@Transient
	public int getTotalChildNodesCount(){
		if(isLeaf())
			return 0;
		else{
			int i = 0;
			for(Node n : childNodes)
				i += n.getTotalChildNodesCount();
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
