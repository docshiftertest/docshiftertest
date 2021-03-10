package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.entities.Node;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */

public interface ChainConfigurationRepository extends CrudRepository<ChainConfiguration, Long> {

	List<ChainConfiguration> findByEnabled(boolean enabled);

	ChainConfiguration findByRootNode(Node rootNode);

	ChainConfiguration findByQueueName(String queueName);

    ChainConfiguration findOneByName(String name);
    
    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.enabled = ?1 where cc.id = ?2")
    void enableWorkFlow(boolean enable, Long id);
    
    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.enabled = ?1")
    void enableOrDisableAllWorkFlows(boolean enable);
    
    @Query("select cc.name FROM ChainConfiguration cc where cc.id = :id")
    String findWorkflowNameById(@Param("id") Long configurationID);
}
