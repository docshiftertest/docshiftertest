package com.docshifter.core.config.domain;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by Den Juleke
 */
	public interface QueueMonitorRepository extends CrudRepository<QueueMonitor, Long> {
	

	public QueueMonitor findByTaskId(String taskId);
}
