package com.docshifter.core.monitoring.repositories;

import com.docshifter.core.monitoring.entities.WebhookConfigurationItem;
import org.javers.spring.annotation.JaversSpringDataAuditable;

/**
 * Created by blazejm on 11.05.2017.
 */
@JaversSpringDataAuditable
public interface WebhookConfigurationItemRepository extends ConfigurationItemRepository<WebhookConfigurationItem> {
}
