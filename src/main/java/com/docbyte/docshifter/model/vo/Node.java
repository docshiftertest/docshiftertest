package com.docbyte.docshifter.model.vo;

import com.docbyte.docshifter.config.test.NodeCallable;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "NODE", schema = "DOCSHIFTER")
public class Node {
	
	private long id;
	private Node parentNode;
	private Set<Node> childNodes=null;
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

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy= GenerationType.AUTO)
	public long getId(){
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "PARENTNODE")
	public Node getParentNode(){
		return parentNode;
	}
	
	public void setParentNode(Node parentNode){
		this.parentNode = parentNode;
	}
	public void addChild(Node n){
		this.childNodes.add(n);
	}


	@OneToMany(mappedBy = "parentNode", fetch = FetchType.EAGER)
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

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "MODULECONFIGURATION")
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
	
	public void iterateOverNode(NodeCallable func){
		func.call(this);
		if(childNodes.size() > 0){
			func.enteringChildNodes();
			for(Node n : childNodes)
				func.call(n);
			func.exitingChildNodes();
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
