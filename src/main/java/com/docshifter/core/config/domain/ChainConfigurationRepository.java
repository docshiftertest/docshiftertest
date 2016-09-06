package com.docshifter.core.config.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
@Repository
public interface ChainConfigurationRepository extends CrudRepository<ChainConfiguration, Long> {

	List<Node> findRootNodesByEnabled(boolean enabled);

	ChainConfiguration findByRootNode(Node rootNode);

	ChainConfiguration findByQueueName(String queueName);
}
