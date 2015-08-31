package com.docbyte.docshifter.model.dao;

import java.util.List;
import java.util.Set;

import com.docbyte.docshifter.model.dao.inter.IParameterDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.Parameter;

public class ParameterDAO implements IParameterDAO
{
	private HibernateTemplateProvider hibernateTemplate;
	
	public ParameterDAO(){
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}
	
	public void delete(Parameter param)
	{
		hibernateTemplate.delete(param);
	}

	public void deleteAll(Set<Parameter> params)
	{
		for(Parameter param : params)
		{
			this.delete(param);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Parameter> find(String name)
	{
		List<Parameter> list = (List<Parameter>) hibernateTemplate.find("from Param p fetch all properties where lower(p.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list;
		else
			return null;
	}

	public Parameter get(Long id)
	{
		return (Parameter) hibernateTemplate.get(Parameter.class, id);
	}

	@SuppressWarnings("unchecked")
	public Parameter get(String name)
	{
		List<Parameter> list = (List<Parameter>) hibernateTemplate.find("from Param p fetch all properties where lower(p.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Parameter> get(){
		return (List<Parameter>) hibernateTemplate.find("from Parameter p");
	}
	
	public void update(Parameter parameter)
	{		
		hibernateTemplate.saveOrUpdate(parameter);
	}

	public Long save(Parameter param)
	{
		hibernateTemplate.saveOrUpdate(param);
		
		return param.getId();
	}

	public void saveAll(Set<Parameter> params)
	{
		for(Parameter param : params)
		{
			this.save(param);
		}
	}

	public HibernateTemplateProvider getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplateProvider hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
}
