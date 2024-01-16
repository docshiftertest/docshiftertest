package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.ChainConfiguration;
import com.docshifter.core.config.entities.Node;
import com.docshifter.core.config.services.ChainConfigurationSample;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */

public interface ChainConfigurationRepository extends CrudRepository<ChainConfiguration, Long> {

	List<ChainConfiguration> findByEnabled(boolean enabled);

	ChainConfiguration findByRootNodes(Node rootNode);

	ChainConfiguration findByRootNodesId(Long id);

	ChainConfiguration findByQueueName(String queueName);

    ChainConfiguration findOneByName(String name);

    @Query("SELECT cc FROM ChainConfiguration cc WHERE cc.id IN :ids")
    List<ChainConfiguration> findByIds(@Param("ids") Set<Long> ids);

    @Modifying(flushAutomatically = true)
    void deleteByIdIn(Set<Long> ids);

    // Only enable (true) workflows that have no broken rules, for disabling (false) we don't care
    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.enabled = ?1 where cc.id in ?2 and (?1 = false or cc.brokenRules is null or cc.brokenRules = '')")
    void enableWorkflowsById(boolean enable, Set<Long> ids);

    // Only enable (true) DSExpress if the workflow has no broken rules, for disabling (false) we don't care
    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.dsexpressEnabled = ?1 where cc.id in ?2 and (?1 = false or cc.brokenRules is null or cc.brokenRules = '')")
    void enableDSExpress(boolean enable, Set<Long> ids);

    // Only enable (true) the workflow if it has no broken rules, for disabling (false) we don't care
    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.enabled = ?1 where cc.id = ?2 and (?1 = false or cc.brokenRules is null or cc.brokenRules = '')")
    void enableWorkFlow(boolean enable, Long id);

    // Only enable (true) all workflows that have no broken rules, for disabling (false) we don't care
    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.enabled = ?1 where ?1 = false or cc.brokenRules is null or cc.brokenRules = ''")
    void enableOrDisableAllWorkFlows(boolean enable);
    
    @Query("select cc.name FROM ChainConfiguration cc where cc.id = :id")
    String findWorkflowNameById(@Param("id") Long configurationID);

    @Query("select cc.name FROM ChainConfiguration cc where cc.enabled = :enabled")
    List<String> findAllWorkflowNameByEnabled(@Param("enabled") boolean enabled);

    @Query("select cc.name FROM ChainConfiguration cc where cc.dsexpressEnabled = :enabled")
    List<String> findAllWorkflowNameByDSExpressEnabledDisabled(@Param("enabled") boolean enabled);

    @Modifying(flushAutomatically = true)
    @Query("update ChainConfiguration cc set cc.dsexpressEnabled = ?1 where ?1 = false or cc.brokenRules is null or cc.brokenRules = ''")
    void enableOrDisableAllWorkFlowsDSExpress(boolean enable);

    List<ChainConfigurationSample> findAllBy();
}
