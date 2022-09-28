package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.Module;
import com.docshifter.core.config.entities.ModuleConfiguration;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
//@Repository
@JaversSpringDataAuditable
public interface ModuleConfigurationRepository extends CrudRepository<ModuleConfiguration, Long> {


    List<ModuleConfiguration> findByModuleId(long id);

    ModuleConfiguration findByName(String name);

    List<ModuleConfiguration> findByModule(Module module);

    List<ModuleConfiguration> findByModuleType(String type);

}
