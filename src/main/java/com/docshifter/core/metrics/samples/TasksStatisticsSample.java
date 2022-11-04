package com.docshifter.core.metrics.samples;

/**
 * @author Created by Juan Marques on 19/08/2021
 */
public interface TasksStatisticsSample {
    long getCount();
    long getSum();
    long getMinimum();
    long getMaximum();
    double getFirstPercentile();
    double getFifthPercentile();
    double getFirstQuartile();
    double getMedian();
    double getThirdQuartile();
    double getNinetyFifthPercentile();
    double getNinetyNinthPercentile();
    double getStandardDeviation();
    double getVariance();
    double getAverage();
}
