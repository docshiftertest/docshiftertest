package com.docshifter.core.metrics.dtos;

import java.util.Map;


/**
 * @author Juan Marques
 * @created 23/01/2023
 * DTO with the data of an Ongoing task to be shown to the user
 * @param id task id
 * @param workflowName name of the workflow in use
 * @param filename name of the file
 * @param taskData data for the task
 * @param status actual status of the task
 */
public record OngoingTaskDTO(
        String id,
        String workflowName,
        String filename,
        Map<String, Object> taskData,
        Status status) {

    public enum Status {
        WAITING_TO_BE_PROCESSED,
        PROCESSING,
        PROCESSED,
    }

    /**
     * Create a new record with the same values and a new {@link Status}
     * @param status {@link Status} to set
     * @return the {@link OngoingTaskDTO} with the provided {@link Status}
     */
    public OngoingTaskDTO withStatus(Status status) {
        return new OngoingTaskDTO(id, workflowName, filename, taskData, status);
    }
}


