package com.docshifter.core.metrics.Sample;

public interface ErrorLogDistributionSample {

    String getTaskId();
    String getSenderHostName();
    String getReceiverHostName();
    String getProcessDate();
    long getProcessDateEpoch();
    String getWorkflowName();
    String getFilename();
    String getTaskMessage();

}
