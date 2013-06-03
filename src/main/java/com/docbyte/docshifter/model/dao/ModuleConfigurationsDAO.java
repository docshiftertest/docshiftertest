package com.docbyte.docshifter.model.dao;

import java.util.ArrayList;
import java.util.List;
import com.docbyte.docshifter.model.dao.inter.IModuleConfigurationsDAO;
import com.docbyte.docshifter.model.util.HibernateTemplateProvider;
import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;
import com.docbyte.docshifter.model.vo.ReceiverConfiguration;
import com.docbyte.docshifter.model.vo.SenderConfiguration;

public class ModuleConfigurationsDAO implements IModuleConfigurationsDAO
{	
	//private static org.apache.log4j.Logger log = Logger.getLogger(ModuleConfigurationsDAO.class);
	
	private HibernateTemplateProvider hibernateTemplate;
	
	public ModuleConfigurationsDAO(){
		hibernateTemplate = HibernateTemplateProvider.getInstance();
	}
	
	@SuppressWarnings("unchecked")
	public void delete(ModuleConfiguration moduleConfiguration) throws IllegalArgumentException
	{
		//check if a sender uses the module configuration
		List<SenderConfiguration> senderList = (List<SenderConfiguration>) hibernateTemplate.find("from SenderConfiguration c where c.inputConfiguration.id = " +moduleConfiguration.getId());
		
		//check if a receiver uses the module configuration
		//TODO: check if still correct
		List<ReceiverConfiguration> receiverList = new ArrayList<ReceiverConfiguration>();
		receiverList.addAll((List<ReceiverConfiguration>) hibernateTemplate.find("from ReceiverConfiguration c where c.transformationConfiguration.id = " +moduleConfiguration.getId()));
		receiverList.addAll((List<ReceiverConfiguration>) hibernateTemplate.find("SELECT c FROM ReceiverConfiguration c, ModuleConfiguration mc WHERE mc IN ELEMENTS(c.releaseConfiguration) AND mc.id = " +moduleConfiguration.getId()));
		
		if(senderList.size() == 0 && receiverList.size() == 0){
			hibernateTemplate.delete(moduleConfiguration);
		}
		else{
			String message = "Module configuration '" +moduleConfiguration.getName() +"' is being used by the following configurations and cannot be deleted:\n";
			for(SenderConfiguration s : senderList){
				message += (" - " +s.getName() +"\n");
			}
			for(ReceiverConfiguration r : receiverList){
				message += (" - " +r.getName() +"\n");
			}
			
			throw new IllegalArgumentException(message);
		}
	}
	
	public ModuleConfiguration get(int id)
	{
		return (ModuleConfiguration) hibernateTemplate.get(ModuleConfiguration.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public ModuleConfiguration get(String name)
	{
		List<ModuleConfiguration> list = (List<ModuleConfiguration>) hibernateTemplate.find(
				"from ModuleConfiguration mc fetch all properties " +
				"where lower(mc.name) = '" + name.toLowerCase() + "'");
		
		if(list.size() >= 1)
			return list.get(0);
		else
			return null; 
	}
	
	@SuppressWarnings("unchecked")
	public List<ModuleConfiguration> getByType(String type)
	{
		List<ModuleConfiguration> list = (List<ModuleConfiguration>) hibernateTemplate.find(
				"from ModuleConfiguration mc fetch all properties " +
				"where lower(mc.module.type) = '" + type.toLowerCase() + "'");
		
		return list;
	}
	
	public void insert(ModuleConfiguration moduleConfiguration) throws IllegalArgumentException
	{		
		if(exists(moduleConfiguration)){
			throw new IllegalArgumentException("A configuration with the same name or parameter values already exists. The configuration cannot be saved.");
		}
		hibernateTemplate.saveOrUpdate(moduleConfiguration);
	}
	
	public void update(ModuleConfiguration moduleConfiguration) throws IllegalArgumentException
	{	
		hibernateTemplate.saveOrUpdate(moduleConfiguration);
	}

	@SuppressWarnings("unchecked")
	public List<ModuleConfiguration> get(Module module) {
		List<ModuleConfiguration> list = (List<ModuleConfiguration>) hibernateTemplate.find(
				"from ModuleConfiguration mc fetch all properties " +
				"where mc.module.id = " + module.getId());
		
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<ModuleConfiguration> get() {
		List<ModuleConfiguration> list = (List<ModuleConfiguration>) hibernateTemplate.find(
				"from ModuleConfiguration mc fetch all properties");
		
		return list;
	}
	
	private boolean exists(ModuleConfiguration config){		
		if(config.getId() == 0){
			List<ModuleConfiguration> list = get();
			
			for(ModuleConfiguration c : list){
				if((c.getParameterValues().equals(config.getParameterValues()) 
						&& c.getModule().equals(config.getModule())) || (c.getName().equals(config.getName()))){
					//exists = true;
					//break;
					
					return true;
				}
			}
			
		}
		return false;
	}
}
