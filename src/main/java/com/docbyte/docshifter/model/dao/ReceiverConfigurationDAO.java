package com.docbyte.docshifter.model.dao;

import java.util.List;
import com.docbyte.docshifter.model.dao.inter.IReceiverConfigurationDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.ReceiverConfiguration;

public class ReceiverConfigurationDAO implements IReceiverConfigurationDAO
{
	HibernateTemplateProvider hibernateTemplate;
	
	public ReceiverConfigurationDAO(){
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}
	
	@SuppressWarnings("unchecked")
	public void delete(ReceiverConfiguration receiverConfiguration)
	{
		List<ChainConfiguration> list = (List<ChainConfiguration>) hibernateTemplate.find("from ChainConfiguration c where c.receiverConfiguration.id = " +receiverConfiguration.getId());
		
		if(list.size() == 0){
			hibernateTemplate.delete(receiverConfiguration);
		}
		else{
			String message = "Receiver configuration '" +receiverConfiguration.getName() +"' is being used by the following transformation configurations and cannot be deleted:\n";
			for(ChainConfiguration c : list){
				message += (" - " +c.getName() +"\n");
			}
			
			throw new UnsupportedOperationException(message);
		}		
	}

	@SuppressWarnings("unchecked")
	public List<ReceiverConfiguration> find(String name)
	{
		List<ReceiverConfiguration> list = (List<ReceiverConfiguration>) hibernateTemplate.find("from ReceiverConfiguration r where lower(r.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list;
		else
			return null;
	}

	public ReceiverConfiguration get(Long id)
	{
		return (ReceiverConfiguration) hibernateTemplate.get(ReceiverConfiguration.class, id);
	}

	@SuppressWarnings("unchecked")
	public ReceiverConfiguration get(String name)
	{
		List<ReceiverConfiguration> list = (List<ReceiverConfiguration>) hibernateTemplate.find("from ReceiverConfiguration r where lower(r.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list.get(0);
		else
			return null;
	}


	public ReceiverConfiguration insert(ReceiverConfiguration receiverConfiguration) throws Exception
	{
		if(exists(receiverConfiguration)){
			throw new Exception("A receiver configuration with the same transformation and release configuration already exists. The configuration cannot be saved.");
		}
		
		hibernateTemplate.merge(receiverConfiguration);
		return receiverConfiguration;
	}

	public ReceiverConfiguration update(ReceiverConfiguration receiverConfiguration)
	{
		hibernateTemplate.merge(receiverConfiguration);
		return receiverConfiguration;
	}
	
	@SuppressWarnings("unchecked")
	public List<ReceiverConfiguration> get() {
		return (List<ReceiverConfiguration>) hibernateTemplate.find("from ReceiverConfiguration r");
	}
	
	private boolean exists(ReceiverConfiguration config){
		boolean exists = false;
		
		if(config.getId() == 0){
			List<ReceiverConfiguration> list = get();
			
			for(ReceiverConfiguration c : list){
				for(ModuleConfiguration trans : c.getTransformationConfiguration().values())
					for(ModuleConfiguration trans2 : config.getTransformationConfiguration().values())	
						if(trans.compareTo(trans2)){
							for(ModuleConfiguration rc : c.getReleaseConfiguration()){
								for(ModuleConfiguration rc2 : config.getReleaseConfiguration()){
									if(rc.equals(rc2)){
										exists = true;
										break;
							}
						}
					}
				}
			}
		}
		
		return exists;
}

	public HibernateTemplateProvider getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplateProvider hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
}
