package com.docbyte.docshifter.model.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.docbyte.docshifter.model.dao.inter.ISenderConfigurationDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.ChainConfiguration;
import com.docbyte.docshifter.model.vo.Parameter;
import com.docbyte.docshifter.model.vo.SenderConfiguration;

public class SenderConfigurationDAO implements ISenderConfigurationDAO
{
	HibernateTemplateProvider hibernateTemplate;
	
	public SenderConfigurationDAO(){
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}
	
	@SuppressWarnings("unchecked")
	public void delete(SenderConfiguration senderConfiguration)
	{
		List<ChainConfiguration> list = (List<ChainConfiguration>) hibernateTemplate.find("from ChainConfiguration c where c.senderConfiguration.id = " +senderConfiguration.getId());
		
		if(list.size() == 0){
			delete(senderConfiguration);
		}
		else{
			String message = "Sender configuration '" +senderConfiguration.getName() +"' is being used by the following transformation configurations and cannot be deleted:\n";
			for(ChainConfiguration c : list){
				message += (" - " +c.getName() +"\n");
			}
			
			throw new UnsupportedOperationException(message);
		}		
	}

	@SuppressWarnings("unchecked")
	public List<SenderConfiguration> find(String name)
	{
		List<SenderConfiguration> list = (List<SenderConfiguration>) hibernateTemplate.find("from SenderConfiguration s where lower(s.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list;
		else
			return null;
	}

	public SenderConfiguration get(Long id)
	{
		return (SenderConfiguration) hibernateTemplate.get(SenderConfiguration.class, id);
	}

	@SuppressWarnings("unchecked")
	public SenderConfiguration get(String name)
	{
		List<SenderConfiguration> list = (List<SenderConfiguration>) hibernateTemplate.find("from SenderConfiguration s where lower(s.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list.get(0);
		else
			return null;
	}

	public void save(SenderConfiguration senderConfiguration)
	{
		hibernateTemplate.saveOrUpdate(senderConfiguration);
	}

	@SuppressWarnings("unchecked")
	public List<SenderConfiguration> get() {
		return (List<SenderConfiguration>) hibernateTemplate.find("from SenderConfiguration s");
	}
	
	@SuppressWarnings("unchecked")
	public List<SenderConfiguration> getEnabled() {
		//return (List<SenderConfiguration>) hibernateTemplate.find("from SenderConfiguration s WHERE s.");
		return (List<SenderConfiguration>) hibernateTemplate.find("SELECT c.senderConfiguration from ChainConfiguration c WHERE c.enabled=true");
	}

	@SuppressWarnings("unchecked")
	public List<SenderConfiguration> getByClassName(String className) {
		List<SenderConfiguration> list = hibernateTemplate.find("SELECT c FROM SenderConfiguration c WHERE c.inputConfiguration.module.classname = '"+className +"'");
		
		return list;
	}
	
	public SenderConfiguration getByClassNameAndParamValue(String className, String paramValue) {
		List<SenderConfiguration> senderConfigs = getByClassName(className);
		SenderConfiguration returnConfig = null;
		
		Iterator<SenderConfiguration> senderIterator = senderConfigs.iterator();
		
		while(senderIterator.hasNext() && returnConfig == null){
			SenderConfiguration sc = senderIterator.next();
			
			Map<Parameter, String> params = sc.getInputConfiguration().getParameterValues();
			Iterator<Parameter> it = params.keySet().iterator();
			
			while(it.hasNext() && returnConfig == null){
				if(params.get(it.next()).equals(paramValue)){
					returnConfig = sc;
				}
			}
		}
		
		return returnConfig;
	}
}
