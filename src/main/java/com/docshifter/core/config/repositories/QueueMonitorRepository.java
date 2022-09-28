package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.QueueMonitor;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Den Juleke
 */
public interface QueueMonitorRepository extends CrudRepository<QueueMonitor, Long> {

	public QueueMonitor findByTaskId(String taskId);
}
