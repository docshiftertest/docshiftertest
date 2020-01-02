package com.docshifter.core.config.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
//@Repository
//@RepositoryRestResource
public interface ChainConfigurationRepository extends CrudRepository<ChainConfiguration, Long> {

	List<ChainConfiguration> findByEnabled(boolean enabled);

	ChainConfiguration findByRootNode(Node rootNode);

	ChainConfiguration findByQueueName(String queueName);

    ChainConfiguration findOneByName(String name);
}
