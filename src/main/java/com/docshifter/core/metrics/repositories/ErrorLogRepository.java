package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.entities.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Julian Isaac on 02.08.2021
 */
public interface ErrorLogRepository extends JpaRepository<ErrorLog, String> {
}
