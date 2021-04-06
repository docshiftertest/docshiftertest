package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.entities.DocumentCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentCounterRepository extends JpaRepository<DocumentCounter, String> {

    @Query("SELECT SUM(c.counts) FROM DocumentCounter c")
    Long selectTotalCounts();

    @Query("SELECT COUNT(c.task_id) FROM DocumentCounter c")
    long selectSuccessfulWorkflows();

}
