package com.docshifter.core.metrics.samples;

/**
 * @author Created by Juan Marques on 19/08/2021
 */
public interface TasksStatisticsSample {
    long getCount();
    Long getSum();
    Long getMinimum();
    Long getMaximum();
    Double getFirstPercentile();
    Double getFifthPercentile();
    Double getFirstQuartile();
    Double getMedian();
    Double getThirdQuartile();
    Double getNinetyFifthPercentile();
    Double getNinetyNinthPercentile();
    Double getStandardDeviation();
    Double getVariance();
    Double getAverage();
}
