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
        chainConfiguration = chainConfigurationRepository.findByRootNodes(n.getRoots().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Expected at least one root node for: " + n)));
        this.moduleWrapper = new ModuleWrapper(n.getModuleConfiguration());
    }
}
