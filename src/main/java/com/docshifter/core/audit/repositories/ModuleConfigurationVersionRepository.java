package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.ModuleConfigurationVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleConfigurationVersionRepository extends JpaRepository<ModuleConfigurationVersion, Long> {

    Optional<ModuleConfigurationVersion> findTopByModuleConfigurationUuidOrderByVersionDesc(UUID uuid);
    List<ModuleConfigurationVersion> findAllByModuleConfigurationUuid(UUID uuid);

    @Query("select distinct mc_version.moduleConfigurationUuid from ModuleConfigurationVersion mc_version")
    List<UUID> findDistinctModuleConfigurationUuid();

    Optional<ModuleConfigurationVersion> findByModuleConfigurationUuidAndVersion(UUID uuid, Integer version);

}
