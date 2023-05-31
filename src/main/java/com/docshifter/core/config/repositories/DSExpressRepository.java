package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.DSExpress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DSExpressRepository extends JpaRepository<DSExpress, Long> {

    @Query("select dse from DSExpress dse where dse.chainConfiguration.id = ?1")
    List<DSExpress> findAllByChainConfigurationId(Long chainConfigurationId);

    @Modifying(flushAutomatically = true)
    @Query("update DSExpress dse set dse.enabled = ?1 where dse.chainConfiguration.id = ?2")
    void enableOrDisable(boolean enable, Long chainConfigurationId);
}
