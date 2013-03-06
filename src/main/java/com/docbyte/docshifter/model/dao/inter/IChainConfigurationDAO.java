package com.docbyte.docshifter.model.dao.inter;

import java.util.List;

import com.docbyte.docshifter.model.vo.ChainConfiguration;

public interface IChainConfigurationDAO
{
	public ChainConfiguration get(Long id);
	public ChainConfiguration get(String name);
	
	public void save(ChainConfiguration config);
	
	public void delete(ChainConfiguration config);
	public List<ChainConfiguration> get();
	public ChainConfiguration getPrintserviceTransformation(String queueName);
}