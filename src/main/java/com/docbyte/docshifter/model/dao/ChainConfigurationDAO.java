package com.docbyte.docshifter.model.dao;

import java.util.ArrayList;
import java.util.List;

import com.docbyte.docshifter.model.dao.inter.IChainConfigurationDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.Node;
import com.docbyte.docshifter.util.Logger;

public class ChainConfigurationDAO implements IChainConfigurationDAO {

	private HibernateTemplateProvider hibernateTemplate;

	public ChainConfigurationDAO() {
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}

	public void delete(ChainConfiguration config) {
		hibernateTemplate.delete(config);
	}

	public ChainConfiguration get(long id) {
		return (ChainConfiguration) hibernateTemplate.get(
				ChainConfiguration.class, id);
	}

	@SuppressWarnings("unchecked")
	public ChainConfiguration get(String name) {
		List<ChainConfiguration> list = (List<ChainConfiguration>) hibernateTemplate
				.find("from ChainConfiguration c fetch all properties "
						+ "where lower(c.name) = '" + name.toLowerCase() + "'");

		if (list.size() >= 1)
			return list.get(0);
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public void save(ChainConfiguration config) throws Exception {
		Logger.info("saving chain conf dao", null);
		if (exists(config)) {
			throw new Exception(
					"A workflow with the same configurations already exists! The workflow cannot be saved.");
		} else if (config.getQueueName().trim().length() > 0) {
			List<ChainConfiguration> list = (List<ChainConfiguration>) hibernateTemplate
					.find("from ChainConfiguration c fetch all properties "
							+ "where lower(c.queueName) = '"
							+ config.getQueueName().toLowerCase()
							+ "' and c.id != " + config.getId());

			if (list.size() > 0) {
				throw new Exception(
						"A transformation configuration with queue name '"
								+ config.getQueueName()
								+ "' already exists. The configuration connot be saved.");
			}
		}
		hibernateTemplate.saveOrUpdate(config);
	}

	@SuppressWarnings("unchecked")
	public void deleteUnusedConfigs() {
		List<Node> unusedNodes = new ArrayList<Node>();
		unusedNodes.addAll(hibernateTemplate.find("select n from Node n where n.parentNode is null AND n not in (select rn from ChainConfiguration cc inner join cc.rootNode rn)"));
		for(Node n : unusedNodes){
			n.clearAllChildNodes();
			hibernateTemplate.saveOrUpdate(n);
			hibernateTemplate.delete(n);
		}
	}

	@SuppressWarnings("unchecked")
	public List<ChainConfiguration> get() {
		return (List<ChainConfiguration>) hibernateTemplate
				.find("from ChainConfiguration c");
	}

	private boolean exists(ChainConfiguration config) {
		List<ChainConfiguration> list = get();
		if (config.getId() == 0) {
			for (ChainConfiguration c : list) {
				if(c.getRootNode().compareTo(config.getRootNode())){
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public ChainConfiguration getPrintserviceTransformation(String queueName) {
		List<ChainConfiguration> list = (List<ChainConfiguration>) hibernateTemplate
				.find("FROM ChainConfiguration c fetch all properties "
						+ "WHERE lower(c.queueName) = '"
						+ queueName.toLowerCase() + "'");

		if (list.size() >= 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public boolean chainExists(ChainConfiguration chainConfiguration) {
		return this.exists(chainConfiguration);
	}

	public void importChain(ChainConfiguration chain) {
		hibernateTemplate.merge(chain);
	}

	public ChainConfiguration load(int id) {
		return (ChainConfiguration) hibernateTemplate.get(
				ChainConfiguration.class, id);
	}

	public HibernateTemplateProvider getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplateProvider hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	@SuppressWarnings("unchecked")
	public ChainConfiguration getByNode(Node n) {
		List<ChainConfiguration> list = (List<ChainConfiguration>) hibernateTemplate
				.find("from ChainConfiguration c where c.rootNode.id = " + n.getId());

		if (list.size() >= 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
}
