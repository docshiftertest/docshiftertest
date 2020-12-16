package com.docshifter.core.monitoring.repo;

import com.docshifter.core.monitoring.entities.Configuration;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by blazejm on 11.05.2017.
 */
public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {
}
