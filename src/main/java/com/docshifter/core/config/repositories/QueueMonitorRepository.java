package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.QueueMonitor;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Den Juleke
 */
@JaversSpringDataAuditable
public interface QueueMonitorRepository extends CrudRepository<QueueMonitor, Long> {

	public QueueMonitor findByTaskId(String taskId);
}
