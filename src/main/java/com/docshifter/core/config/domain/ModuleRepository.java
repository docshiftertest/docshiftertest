package com.docshifter.core.config.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
//@Repository
//@RepositoryRestResource
public interface ModuleRepository extends CrudRepository<Module, Long> {


	Module findOneByName(String name);

    List<Module> findByName(String name);

    List<Module> findByType(String type);
}