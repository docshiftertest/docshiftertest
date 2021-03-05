package com.docshifter.core.metric.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MetricRepository extends JpaRepository<Metric, String> {
}
