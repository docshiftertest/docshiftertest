package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.ModuleConfigurationVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ModuleConfigurationVersionRepository extends JpaRepository<ModuleConfigurationVersion, Long> {

    Optional<ModuleConfigurationVersion> findTopByModuleConfigurationUuidOrderByVersionDesc(UUID uuid);

}
