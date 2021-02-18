package com.docshifter.core.metric.services;

import com.docshifter.core.metric.MetricDto;

import java.io.IOException;

public interface MetricService {
    void storeMetric(int count);

    MetricDto createMetricDto(String filename);
}
