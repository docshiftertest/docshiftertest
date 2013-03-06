package com.docbyte.docshifter.model.dao;

import java.util.List;
import com.docbyte.docshifter.model.dao.inter.IReceiverConfigurationDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
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
			delete(receiverConfiguration);
		}
		else{
			String message = "Receiver configuration '" +receiverConfiguration.getName() +"' is being used by the following transformation configurations and cannot be deleted:\n";
			for(ChainConfiguration c : list){
				message += (" - " +c.getName() +"\n");
			}
			
			throw new UnsupportedOperationException(message);
		}		
	}

	public List<ReceiverConfiguration> find(String name)
	{
		List<ReceiverConfiguration> list = (List<ReceiverConfiguration>) find("from ReceiverConfiguration r where lower(r.name) = '" + name.toLowerCase() + "'");
		
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

	public void save(ReceiverConfiguration receiverConfiguration)
	{
		hibernateTemplate.saveOrUpdate(receiverConfiguration);
	}

	@SuppressWarnings("unchecked")
	public List<ReceiverConfiguration> get() {
		return (List<ReceiverConfiguration>) hibernateTemplate.find("from ReceiverConfiguration r");
	}
}
