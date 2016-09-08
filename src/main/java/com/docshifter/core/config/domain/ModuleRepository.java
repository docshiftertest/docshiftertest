package com.docshifter.core.config.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
@Repository
@RepositoryRestResource
public interface ModuleRepository extends CrudRepository<Module, Long> {


	Module findOneByName(String name);
}