package com.docshifter.core.config.wrapper;


import com.docshifter.core.config.repositories.ChainConfigurationRepository;
import com.docshifter.core.config.entities.Node;

public class ReceiverConfigurationWrapper extends NodeWrapper {

	public ReceiverConfigurationWrapper(Node node, ChainConfigurationRepository chainConfigurationRepository) {
		super(node, chainConfigurationRepository);
	}

}
