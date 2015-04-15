package com.docbyte.docshifter.model.vo;

import java.util.Set;

public class Node {
	
	private int id;
	private Node parentNode;
	private Set<Node> childNodes;
	private ModuleConfiguration moduleConfiguration;
	
	public Node(){}
	
	public Node(Node parentNode, ModuleConfiguration moduleConfiguration){
		this.parentNode = parentNode;
		this.moduleConfiguration = moduleConfiguration;
	}
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public Node getParentNode(){
		return parentNode;
	}
	
	public void setParentNode(Node parentNode){
		this.parentNode = parentNode;
	}
	
	public Set<Node> getChildNodes(){
		return childNodes;
	}
	
	public void setChildNodes(Set<Node> childNodes){
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
}
