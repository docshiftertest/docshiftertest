package com.docshifter.core.metrics.dtos;

import java.util.Map;

/**
 * @author Juan Marques
 * @created 23/01/2023
 */
public record OngoingTaskDTO(String id, String workflowName, String filename, Map<String, Object> taskData,
                             Status status) {

    public enum Status {
        WAITING_TO_BE_PROCESSED,
        PROCESSING,
        PROCESSED,
    }
}


