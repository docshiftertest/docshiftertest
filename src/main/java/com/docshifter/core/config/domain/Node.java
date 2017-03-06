package com.docshifter.core.config.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Entity
public class Node {
	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;

	@ManyToOne
	@JsonIgnore()
	private Node parentNode;

	//@OneToMany(mappedBy = "parentNode")
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parentNode", cascade = CascadeType.ALL)
	private Set<Node> childNodes=null;

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
			this.childNodes=new HashSet<Node>();
		}
		this.moduleConfiguration = moduleConfiguration;
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
		if(this.childNodes==null)
		{
			this.childNodes=childNodes;
		}
		else{
			this.childNodes.clear();
			childNodes.addAll(childNodes);
		}
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
		if(childNodes.size() == 0)
			return 0;
		else{
			int i = 0;
			for(Node n : childNodes)
				i += n.getTotalChildNodesCount();
			return i;
		}
	}

	@Transient
	public boolean isLeaf(){
		return childNodes.size() == 0;
	}
}
