package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.entities.DocumentCounter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricRepository extends JpaRepository<DocumentCounter, String> {
}
