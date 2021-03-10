package com.docshifter.core.metrics.services;

import com.docshifter.core.metrics.dtos.DocumentCounterDTO;

public interface MetricService {
    void storeMetric(int count);

    DocumentCounterDTO createMetricDto(String filename);
}
