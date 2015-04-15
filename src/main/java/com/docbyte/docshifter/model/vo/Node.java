package com.docbyte.docshifter.model.vo;

import java.util.Set;
import java.util.concurrent.Callable;

import com.docbyte.docshifter.config.test.NodeCallable;

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
	
	public void iterateOverNode(NodeCallable func){
		func.call(this);
		if(childNodes.size() > 0){
			func.enteringChildNodes();
			for(Node n : childNodes)
				func.call(n);
			func.exitingChildNodes();
		}
	}
	
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
}
