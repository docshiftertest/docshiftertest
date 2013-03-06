package com.docbyte.docshifter.model.dao.inter;

import java.util.List;

import com.docbyte.docshifter.model.vo.ReceiverConfiguration;

public interface IReceiverConfigurationDAO
{
	public ReceiverConfiguration get(Long id);
	public ReceiverConfiguration get(String name);
	public List<ReceiverConfiguration> get();

	public List<ReceiverConfiguration> find(String name);
	
	public void save(ReceiverConfiguration receiverConfiguration);
	
	public void delete(ReceiverConfiguration receiverConfiguration); 
}
