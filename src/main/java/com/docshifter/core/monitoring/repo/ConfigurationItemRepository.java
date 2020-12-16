package com.docshifter.core.monitoring.repo;

import com.docshifter.core.monitoring.entities.AbstractConfigurationItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ConfigurationItemRepository<T extends AbstractConfigurationItem> extends CrudRepository<T, Long> {
    List<T> findByConfigurationId(long configurationId);
}