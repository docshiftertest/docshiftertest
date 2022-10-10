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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
public class Node {
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne(cascade = CascadeType.REMOVE)
	@JsonIgnore()
	private Node parentNode;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parentNode", cascade = CascadeType.ALL)
	private Set<Node> childNodes=null;

	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToOne
	private ModuleConfiguration moduleConfiguration;

	public Node(){}
	
	public Node(Node parentNode, ModuleConfiguration moduleConfiguration){
		setParentNode(parentNode);
		if(parentNode!=null)
		{
			this.parentNode.addChild(this);
		}
		if(childNodes==null)
		{
			this.childNodes=new HashSet<>();
		}
		this.moduleConfiguration = moduleConfiguration;
	}

	private Node deepCopyParent() {
		if (isRoot()) {
			return new Node(null, moduleConfiguration);
		}
		return new Node(parentNode.deepCopyParent(), moduleConfiguration);
	}

	public Node deepCopy() {
		Node copiedNode = deepCopyParent();
		deepCopyChildren(copiedNode);
		return copiedNode;
	}

	private void deepCopyChildren(Node copiedNode) {
		for (Node childNode : childNodes) {
			childNode.deepCopyChildren(new Node(copiedNode, childNode.moduleConfiguration));
		}
	}

	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}


	public Node getParentNode(){
		return parentNode;
	}
	
	public void setParentNode(Node parentNode){
		this.parentNode = parentNode;
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
		if(o instanceof Node){
			
			Node node = (Node)o;
			if(node.childNodes.size() != childNodes.size())
				return false;
			if(node.parentNode != parentNode)
				return false;
			
			for(Node child : childNodes){
				boolean exists = false;
				for(Node otherChild : node.childNodes)
					if(child.compareTo(otherChild)){
						exists = true;
						break;
					}
				if(!exists)
					return false;
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
		return parentNode == null;
	}

	@Transient
	@JsonIgnore
	public Node getRoot() {
		Node currNode = this;
		while (!currNode.isRoot()) {
			currNode = currNode.parentNode;
		}
		return currNode;
	}

	@Transient
	public boolean isLeaf(){
		return childNodes.size() == 0;
	}
}
