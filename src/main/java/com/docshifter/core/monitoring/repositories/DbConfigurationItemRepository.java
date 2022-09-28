package com.docshifter.core.monitoring.repositories;

import com.docshifter.core.monitoring.entities.DbConfigurationItem;
import org.javers.spring.annotation.JaversSpringDataAuditable;

/**
 * Created by blazejm on 29.09.2017.
 */
//@Repository
@JaversSpringDataAuditable
public interface DbConfigurationItemRepository extends ConfigurationItemRepository<DbConfigurationItem> {
}
