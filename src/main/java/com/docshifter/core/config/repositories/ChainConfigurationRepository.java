package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.services.ChainConfigurationSample;
import com.docshifter.core.config.entities.Node;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */

@JaversSpringDataAuditable
public interface ChainConfigurationRepository extends CrudRepository<ChainConfiguration, Long> {

	List<ChainConfiguration> findByEnabled(boolean enabled);

	ChainConfiguration findByRootNode(Node rootNode);

	ChainConfiguration findByQueueName(String queueName);

    ChainConfiguration findOneByName(String name);

    @Query("SELECT cc FROM ChainConfiguration cc WHERE cc.id IN :ids")
    List<ChainConfiguration> findByIds(@Param("ids") Set<Long> ids);

    @Modifying(flushAutomatically = true)
    void deleteByIdIn(Set<Long> ids);

    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.enabled = ?1 where cc.id in ?2")
    void enableWorkflowsById(boolean enable,Set<Long> ids);
    
    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.enabled = ?1 where cc.id = ?2")
    void enableWorkFlow(boolean enable, Long id);
    
    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.enabled = ?1")
    void enableOrDisableAllWorkFlows(boolean enable);
    
    @Query("select cc.name FROM ChainConfiguration cc where cc.id = :id")
    String findWorkflowNameById(@Param("id") Long configurationID);

    List<ChainConfigurationSample> findAllBy();
}
