package com.docbyte.docshifter.model.dao;

import java.util.List;
import com.docbyte.docshifter.model.dao.inter.IChainConfigurationDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.ChainConfiguration;

public class ChainConfigurationDAO implements IChainConfigurationDAO{
	
	private HibernateTemplateProvider hibernateTemplate;
	
	public ChainConfigurationDAO(){
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}
	
	public void delete(ChainConfiguration config)
	{
		hibernateTemplate.delete(config);
	}

	public ChainConfiguration get(Long id)
	{
		return (ChainConfiguration) hibernateTemplate.get(ChainConfiguration.class, id);
	}

	@SuppressWarnings("unchecked")
	public ChainConfiguration get(String name)
	{
		List<ChainConfiguration> list = (List<ChainConfiguration>) hibernateTemplate.find(
				"from ChainConfiguration c fetch all properties " +
				"where lower(c.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list.get(0);
		else
			return null; 
	}

	public void save(ChainConfiguration config)
	{
		hibernateTemplate.saveOrUpdate(config);
	}

	@SuppressWarnings("unchecked")
	public List<ChainConfiguration> get() {
		return (List<ChainConfiguration>) hibernateTemplate.find("from ChainConfiguration c");
	}

	@SuppressWarnings("unchecked")
	public ChainConfiguration getPrintserviceTransformation(String queueName) {
		List<ChainConfiguration> list = (List<ChainConfiguration>) hibernateTemplate.find(
				"FROM ChainConfiguration c fetch all properties " +
				"WHERE lower(c.queueName) = '" +queueName.toLowerCase() +"'");
		
		if(list.size() >= 0){
			return list.get(0);
		}
		else{
			return null;
		}
	}
}
