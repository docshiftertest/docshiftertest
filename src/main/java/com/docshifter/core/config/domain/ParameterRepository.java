package com.docshifter.core.config.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
@Repository
@RepositoryRestResource
public interface ParameterRepository extends CrudRepository<Parameter, Long> {


    List<Parameter> findByName(String name);

    Parameter findOneByName(String name);
}