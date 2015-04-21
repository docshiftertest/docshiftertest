package com.docbyte.docshifter.model.dao.inter;

import java.util.List;

import com.docbyte.docshifter.model.vo.ChainConfiguration;

public interface IChainConfigurationDAO
{
	public ChainConfiguration get(long id);
	public ChainConfiguration get(String name);
	
	public void save(ChainConfiguration config) throws Exception;
	
	public void delete(ChainConfiguration config);
	public List<ChainConfiguration> get();
	public ChainConfiguration getPrintserviceTransformation(String queueName);
	boolean chainExists(ChainConfiguration chainConfiguration);
	void importChain(ChainConfiguration chain);
	public void deleteUnusedConfigs();
}