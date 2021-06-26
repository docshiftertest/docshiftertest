package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.Module;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
public interface ModuleRepository extends CrudRepository<Module, Long> {

	Module findOneByName(String name);

	Module findOneByClassname(String classname);

    List<Module> findByName(String name);

    List<Module> findByType(String type);
}
