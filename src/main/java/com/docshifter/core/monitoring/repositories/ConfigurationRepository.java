package com.docshifter.core.monitoring.repositories;

import com.docshifter.core.monitoring.entities.Configuration;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

/**
 * Created by blazejm on 11.05.2017.
 */
@JaversSpringDataAuditable
public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {

    @Modifying(flushAutomatically = true)
    void deleteByIdIn(Set<Long> ids);
}
