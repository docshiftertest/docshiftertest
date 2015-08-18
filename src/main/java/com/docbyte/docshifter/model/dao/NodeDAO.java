package com.docbyte.docshifter.model.dao;

import com.docbyte.docshifter.model.dao.inter.INodeDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.Node;
import com.docbyte.docshifter.model.vo.Parameter;
import com.docbyte.docshifter.util.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NodeDAO implements INodeDAO
{
	HibernateTemplateProvider hibernateTemplate;
	
	public NodeDAO(){
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}
	
	@SuppressWarnings("unchecked")
	public List<Node> get() {
		return (List<Node>) hibernateTemplate.find("from Node n");
	}
	

	public HibernateTemplateProvider getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplateProvider hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	@Override
	public Node get(long id) {
		return (Node) hibernateTemplate.get(Node.class, id);
	}

	@Override
	public Node insert(Node node) throws IllegalArgumentException {
		if(exists(node))
			throw new IllegalArgumentException("A node with the same children already exists. The node cannot be saved.");
		hibernateTemplate.merge(node);
		return node;
	}

	@Override
	public Node update(Node node) {
		hibernateTemplate.merge(node);
		return node;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete(Node node) {
		Node currentNode = node;
		while(currentNode.getParentNode() != null){
			currentNode = currentNode.getParentNode();
		}
		// We are now working with the highest node in our nodetree
		List<ChainConfiguration> configs = (List<ChainConfiguration>)hibernateTemplate.find("from ChainConfiguration cc where cc.rootNode.id = " + currentNode.getId());
		if(configs.size() == 0){
			hibernateTemplate.delete(node);
		}else{
			String message = "Node is being used by the following chain configurations and cannot be deleted:\n";
			for(ChainConfiguration c : configs){
				message += (" - " +c.getName() +"\n");
			}
			
			throw new UnsupportedOperationException(message);
		}
		
	}
	
	private boolean exists(Node node){
		if(node.getId() == 0){
			Node currentNode = node.getParentNode();
			while(currentNode != null){
				if(currentNode.getModuleConfiguration() == node.getModuleConfiguration())
					return true;
				currentNode = currentNode.getParentNode();
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<Node> getEnabledSenderConfigurations() {
		//List<Node> nodes = (List<Node>)hibernateTemplate.find("select n from Node n inner join n.moduleConfiguration mc inner join mc.module m where m.type = 'Input'");
		List<Node> nodes = (List<Node>)hibernateTemplate.find("select n from ChainConfiguration cc inner join cc.rootNode n inner join n.moduleConfiguration mc inner join mc.module m where cc.enabled = true");
		Logger.info(nodes.size()+"", null);
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	public List<Node> getSendersByClassName(String className) {
		List<Node> list = (List<Node>)hibernateTemplate.find("select n from Node n where n.moduleConfiguration.module.classname = '" + className + "'");
		
		return list;
	}

	public Node getSenderByClassNameAndParamValue(String className, String paramValue) {
		List<Node> list = getSendersByClassName(className);
		Node node = null;
		Iterator<Node> senderIterator = list.iterator();
		
		while(senderIterator.hasNext() && node == null){
			Node n = senderIterator.next();
			Map<Parameter, String> params = n.getModuleConfiguration().getParameterValues();
			Iterator<Parameter> it = params.keySet().iterator();
			
			while(it.hasNext() && node == null){
				if(params.get(it.next()).equals(paramValue)){
					node = n;
				}
			}
		}
		return node;
		
	}
}
