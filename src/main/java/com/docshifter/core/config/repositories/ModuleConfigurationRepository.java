package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.Module;
import com.docshifter.core.config.entities.ModuleConfiguration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
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

    ModuleConfiguration findOneByName(String name);

    @Query("SELECT mc FROM ModuleConfiguration mc WHERE mc.id IN :ids")
    List<ModuleConfiguration> findByIds(@Param("ids") Set<Long> ids);

    List<ModuleConfiguration> findAllBy();

    @Query(value = "select mc.* " +
            "from docshifter.module_configuration mc " +
            "join docshifter.module m on mc.module_id = m.id " +
            "where m.name in (:moduleNameList)", nativeQuery = true)
    List<ModuleConfiguration> findAllByModuleNameList(@Param("moduleNameList") List<String> moduleNameList);
}
