package com.docshifter.core.config.wrapper;


import com.docshifter.core.config.domain.ChainConfigurationRepository;
import com.docshifter.core.config.domain.Node;

public class ReceiverConfigurationWrapper extends NodeWrapper {

	public ReceiverConfigurationWrapper(Node n, ChainConfigurationRepository chainConfigurationRepository) {
		super(n, chainConfigurationRepository);
	}

}
