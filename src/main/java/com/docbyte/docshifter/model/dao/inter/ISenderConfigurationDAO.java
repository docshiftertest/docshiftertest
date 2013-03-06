package com.docbyte.docshifter.model.dao.inter;

import java.util.List;
import com.docbyte.docshifter.model.vo.SenderConfiguration;

public interface ISenderConfigurationDAO
{
	public SenderConfiguration get(Long id);
	public SenderConfiguration get(String name);
	public List<SenderConfiguration> getByClassName(String className);
	
	public List<SenderConfiguration> find(String name);
	
	public void save(SenderConfiguration senderConfiguration);
	
	public void delete(SenderConfiguration senderConfiguration);
	public List<SenderConfiguration> get();
}
