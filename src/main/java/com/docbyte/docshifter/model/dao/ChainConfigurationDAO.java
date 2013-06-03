package com.docbyte.docshifter.model.dao;

import java.util.List;
import com.docbyte.docshifter.model.dao.inter.IChainConfigurationDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.ReceiverConfiguration;

public class ChainConfigurationDAO implements IChainConfigurationDAO{
	
	private HibernateTemplateProvider hibernateTemplate;
	
	public ChainConfigurationDAO(){
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}
	
	public void delete(ChainConfiguration config)
	{
		hibernateTemplate.delete(config);
	}

	public ChainConfiguration get(int id)
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

	@SuppressWarnings("unchecked")
	public void save(ChainConfiguration config) throws Exception
	{
		if(exists(config)){
			throw new Exception("A workflow with the same configurations already exists! The workflow cannot be saved.");
		}
		else if(config.getQueueName().trim().length() > 0){
			List<ChainConfiguration> list = (List<ChainConfiguration>) hibernateTemplate.find("from ChainConfiguration c fetch all properties " +
				"where lower(c.queueName) = '" + config.getQueueName().toLowerCase() + "' and c.id != " +config.getId());
			
			if(list.size() > 0){
				throw new Exception("A transformation configuration with queue name '" + config.getQueueName() +"' already exists. The configuration connot be saved.");
			}
		}
		hibernateTemplate.saveOrUpdate(config);
	}

	@SuppressWarnings("unchecked")
	public List<ChainConfiguration> get() {
		return (List<ChainConfiguration>) hibernateTemplate.find("from ChainConfiguration c");
	}
	
	private boolean exists(ChainConfiguration config){
		List<ChainConfiguration> list = get();
		boolean exists = false;
		
		if(config.getId() == 0){
			for(ChainConfiguration c : list){
				for(ReceiverConfiguration rec:c.getReceiverConfiguration())
					if(c.getSenderConfiguration().compareTo(config.getSenderConfiguration()) && 
						rec.compareTo(config.getReceiverConfiguration())){
						
						exists = true;
						break;
				}
			}
		}
		
		return exists;
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
