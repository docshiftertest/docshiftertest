package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.ChainConfigurationVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChainConfigurationVersionRepository extends JpaRepository<ChainConfigurationVersion, Long> {

    List<ChainConfigurationVersion> findAllByChainConfigurationUuid(UUID uuid);

    Optional<ChainConfigurationVersion> findTopByChainConfigurationUuidOrderByVersionDesc(UUID uuid);

    Optional<ChainConfigurationVersion> findByChainConfigurationUuidAndVersion(UUID uuid, Integer version);

}
