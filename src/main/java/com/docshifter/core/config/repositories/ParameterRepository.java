package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.Parameter;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
public interface ParameterRepository extends CrudRepository<Parameter, Long> {


    List<Parameter> findByName(String name);

    Parameter findOneByName(String name);
}
