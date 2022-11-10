package com.docshifter.core.metrics.samples;

/**
 * @author Created by Juan Marques on 17/08/2021
 */
public interface ProcessedTasksSample {
    String getWorkflowName();
    Boolean getSuccess();
    Long getCount();
}
