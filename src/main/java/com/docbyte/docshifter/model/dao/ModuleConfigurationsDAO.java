package com.docbyte.docshifter.model.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.docbyte.docshifter.model.dao.inter.IModuleConfigurationsDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.Node;
import com.docbyte.docshifter.util.Logger;

public class ModuleConfigurationsDAO implements IModuleConfigurationsDAO {
	// private static org.apache.log4j.Logger log =
	// Logger.getLogger(ModuleConfigurationsDAO.class);

	private HibernateTemplateProvider hibernateTemplate;

	public ModuleConfigurationsDAO() {
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}

	@SuppressWarnings("unchecked")
	public void delete(ModuleConfiguration moduleConfiguration)
			throws IllegalArgumentException {
		
		List<Node> nodeList = (List<Node>)hibernateTemplate.find("from Node n where n.moduleConfiguration.id = " + moduleConfiguration.getId());
		if(nodeList.size() == 0)
			hibernateTemplate.delete(moduleConfiguration);
		else{
			String message = "Module configuration '"
					+ moduleConfiguration.getName()
					+ "' is being used by the following nodes and cannot be deleted:\n";
			for (Node n : nodeList) {
				message += (" - id:" + n.getId() + "\n");
			}
			throw new IllegalArgumentException(message);
		}
	}

	public ModuleConfiguration get(int id) {
		return (ModuleConfiguration) hibernateTemplate.get(
				ModuleConfiguration.class, id);
	}

	@SuppressWarnings("unchecked")
	public ModuleConfiguration get(String name) {
		List<ModuleConfiguration> list = (List<ModuleConfiguration>) hibernateTemplate
				.find("from ModuleConfiguration mc fetch all properties "
						+ "where lower(mc.name) = '" + name.toLowerCase() + "'");

		if (list.size() >= 1)
			return list.get(0);
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public List<ModuleConfiguration> getByType(String type) {
		List<ModuleConfiguration> list = (List<ModuleConfiguration>) hibernateTemplate
				.find("from ModuleConfiguration mc fetch all properties "
						+ "where lower(mc.module.type) = '"
						+ type.toLowerCase() + "'");

		return list;
	}

	public void insert(ModuleConfiguration moduleConfiguration)
			throws IllegalArgumentException {
		if (exists(moduleConfiguration)) {
			throw new IllegalArgumentException(
					"A configuration with the same name or parameter values already exists. The configuration cannot be saved.");
		}
		hibernateTemplate.saveOrUpdate(moduleConfiguration);
	}

	public void update(ModuleConfiguration moduleConfiguration)
			throws IllegalArgumentException {
		hibernateTemplate.saveOrUpdate(moduleConfiguration);
	}

	@SuppressWarnings("unchecked")
	public List<ModuleConfiguration> get(Module module) {
		List<ModuleConfiguration> list = (List<ModuleConfiguration>) hibernateTemplate
				.find("from ModuleConfiguration mc fetch all properties "
						+ "where mc.module.id = " + module.getId());

		return list;
	}

	@SuppressWarnings("unchecked")
	public List<ModuleConfiguration> get() {
		List<ModuleConfiguration> list = (List<ModuleConfiguration>) hibernateTemplate
				.find("from ModuleConfiguration mc fetch all properties");

		return list;
	}

	private boolean exists(ModuleConfiguration config) {
		if (config.getId() == 0) {
			List<ModuleConfiguration> list = get();

			
			for(ModuleConfiguration c : list){
				if(/*(c.getParameterValues().equals(config.getParameterValues()) 
						&& c.getModule().equals(config.getModule())) || */(c.getName().equals(config.getName()))){
					//exists = true;
					//break;

					return true;
				}
			}
		}
		return false;
	}

	public ModuleConfiguration canImport(ModuleConfiguration config) {
		if (config.getId() == 0) {
			List<ModuleConfiguration> list = get(config.getModule());

			for (ModuleConfiguration c : list) {
				// parameters equal always use the one from database
				if ((c.getParameterValues().equals(config.getParameterValues()) && c
						.getModule().equals(config.getModule()))) {
					return c;
				}
				// Same name save change configname to random new name but do
				// not return, same parameter still possible
			}

			if (this.get(config.getName()) != null) {
				config.setName("cConfig" + (new Date()).getTime());
			}
		}
		return config;
	}

	public void importConfiguration(ModuleConfiguration config) {
		hibernateTemplate.saveOrUpdate(config);
	}

	public HibernateTemplateProvider getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplateProvider hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
}
