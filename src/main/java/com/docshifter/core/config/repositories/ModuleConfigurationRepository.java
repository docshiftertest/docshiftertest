package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.Module;
import com.docshifter.core.config.entities.ModuleConfiguration;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
//@Repository
public interface ModuleConfigurationRepository extends CrudRepository<ModuleConfiguration, Long> {

    List<ModuleConfiguration> findByModuleId(long id);

    ModuleConfiguration findByName(String name);

    List<ModuleConfiguration> findByModule(Module module);

    List<ModuleConfiguration> findByModuleType(String type);

    ModuleConfiguration findByUuid(UUID uuid);

}
