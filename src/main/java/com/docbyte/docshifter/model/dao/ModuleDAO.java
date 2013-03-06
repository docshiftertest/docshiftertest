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
	
	public Module get(Long id)
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

	public Long save(Module module)
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
}
