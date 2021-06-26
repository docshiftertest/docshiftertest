package com.docshifter.core.config.wrapper;

import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.repositories.ChainConfigurationRepository;
import com.docshifter.core.config.entities.Node;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class NodeWrapper {

    private Node node;
    private ChainConfiguration chainConfiguration;
    private ModuleWrapper moduleWrapper;

    protected ChainConfigurationRepository chainConfigurationRepository;

    public NodeWrapper(Node n, ChainConfigurationRepository chainConfigurationRepository) {
        this.node = n;
        this.chainConfigurationRepository = chainConfigurationRepository;

        Node rootNode = n;
        if (n != null) {
            while (rootNode.getParentNode() != null)
                rootNode = rootNode.getParentNode();
        }
        chainConfiguration = chainConfigurationRepository.findByRootNode(rootNode);
        this.moduleWrapper = new ModuleWrapper(n.getModuleConfiguration());
    }
}
