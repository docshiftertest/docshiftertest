package com.docbyte.docshifter.model.dao.inter;

import java.util.List;

import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;

public interface IModuleConfigurationsDAO
{
	public ModuleConfiguration get(Long id);
	public ModuleConfiguration get(String name);

	public void save(ModuleConfiguration moduleConfiguration);
	
	public void delete(ModuleConfiguration moduleConfiguration) throws UnsupportedOperationException;
	
	public List<ModuleConfiguration> get(Module module);
	public List<ModuleConfiguration> getByType(String type);
	public List<ModuleConfiguration> get();
}
