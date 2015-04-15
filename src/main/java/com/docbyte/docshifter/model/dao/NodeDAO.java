package com.docbyte.docshifter.model.dao;

import java.util.List;

import com.docbyte.docshifter.model.dao.inter.INodeDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.Node;

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
	public Node get(Long id) {
		return (Node) hibernateTemplate.get(Node.class, id);
	}

	@Override
	public Node insert(Node node) throws Exception {
		if(exists(node))
			throw new Exception("A node with the same children already exists. The node cannot be saved.");
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
		while(node.getParentNode() != null)
			currentNode = currentNode.getParentNode();
		
		// We are now working with the highest node in our nodetree
		List<ChainConfiguration> configs = (List<ChainConfiguration>)hibernateTemplate.find("from ChainConfiguration cc where cc.rootNode.id = " + currentNode.getId());
		if(configs.size() == 0){
			node.clearAllChildNodes();
			hibernateTemplate.saveOrUpdate(node);
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
	public List<Node> getEnabledSenderNodes() {
		List<Node> nodes = (List<Node>)hibernateTemplate.find("select n from Node n inner join n.moduleConfiguration mc inner join mc.module m where m.type = 'sender'");
		return nodes;
	}
}
