package com.docshifter.core.monitoring.repositories;

import com.docshifter.core.monitoring.entities.SnmpConfigurationItem;
import org.javers.spring.annotation.JaversSpringDataAuditable;

/**
 * Created by blazejm on 11.05.2017.
 */
//@Repository
@JaversSpringDataAuditable
public interface SnmpConfigurationItemRepository extends ConfigurationItemRepository<SnmpConfigurationItem> {
}
