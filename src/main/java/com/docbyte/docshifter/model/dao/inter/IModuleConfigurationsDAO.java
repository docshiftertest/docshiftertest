package com.docbyte.docshifter.model.dao.inter;

import java.util.List;

import com.docbyte.docshifter.model.vo.Module;
import com.docbyte.docshifter.model.vo.ModuleConfiguration;

public interface IModuleConfigurationsDAO
{
	public ModuleConfiguration get(int id);
	public ModuleConfiguration get(String name);

	public void insert(ModuleConfiguration moduleConfiguration) throws IllegalArgumentException;
	public void update(ModuleConfiguration moduleConfiguration) throws IllegalArgumentException;
	
	public void delete(ModuleConfiguration moduleConfiguration) throws IllegalArgumentException;
	
	public List<ModuleConfiguration> get(Module module);
	public List<ModuleConfiguration> getByType(String type);
	public List<ModuleConfiguration> get();
	
	public ModuleConfiguration canImport(ModuleConfiguration config);
	public void importConfiguration(ModuleConfiguration config);
}
