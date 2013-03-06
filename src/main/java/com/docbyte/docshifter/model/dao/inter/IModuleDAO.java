package com.docbyte.docshifter.model.dao.inter;

import java.util.List;

import com.docbyte.docshifter.model.vo.Module;

public interface IModuleDAO
{
	public Module get(Long id);
	public Module get(String name);
	public List<Module> getModules();
	
	public List<Module> find(String name);
	
	public Long save(Module module);
	
	public void delete(Module module);
	public List<Module> getModulesByType(String type);
}
