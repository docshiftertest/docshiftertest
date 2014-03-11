package com.docbyte.docshifter.model.dao;

import java.util.List;

import com.docbyte.docshifter.model.dao.inter.IModuleDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.Module;

public class ModuleDAO implements IModuleDAO
{
	private HibernateTemplateProvider hibernateTemplate;
	
	public ModuleDAO(){
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}
	
	public void delete(Module module)
	{
		hibernateTemplate.delete(module);
	}

	@SuppressWarnings("unchecked")
	public List<Module> find(String name)
	{
		List<Module> list = (List<Module>) hibernateTemplate.find("from Module m fetch all properties where lower(m.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list;
		else
			return null;
	}
	
	public Module get(int id)
	{
		return (Module) hibernateTemplate.get(Module.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public Module get(String name)
	{
		List<Module> list = (List<Module>) hibernateTemplate.find("from Module m fetch all properties where lower(m.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list.get(0);
		else
			return null; 
	}

	public int insert(Module module) throws IllegalArgumentException
	{
		if(exists(module)){
			throw new IllegalArgumentException("A module with class name '" +module.getClassname() +"' already exists. The module cannot be saved.");
		}
		
		hibernateTemplate.saveOrUpdate(module);
		return module.getId();
	}
	
	public int update(Module module) throws IllegalArgumentException
	{		
		hibernateTemplate.saveOrUpdate(module);
		return module.getId();
	}
	
	@SuppressWarnings("unchecked")
	public List<Module> getModules(){
		return (List<Module>) hibernateTemplate.find("from Module m");
	}

	@SuppressWarnings("unchecked")
	public List<Module> getModulesByType(String type) {
		return (List<Module>) hibernateTemplate.find("from Module m fetch all properties where m.type = '" + type + "'");
	}
	
	@SuppressWarnings("unchecked")
	private boolean exists(Module module){
		if(module.getId() == 0){
			List<Module> list = (List<Module>) hibernateTemplate.find("from Module m fetch all properties where m.classname = '" + module.getClassname() + "'");
		
			return (list.size() != 0);
		}
		else{
			return false;
		}
	}

	
	public HibernateTemplateProvider getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplateProvider hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
}
