package com.docshifter.core.metrics.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
Data transfer object to store metrics
 Currently stores filename and counts, will be expanded with other metrics
 Metrics other than the basic counter should be implemented as a licensable module
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCounterDTO {

    private String task_id;
    private long counts;

   //non-fugly Camel_Case_Underscore methods; @Getter and @Setter create these automatically otherwise
    public void setTaskId(String taskId){ this.task_id = taskId;}

    public String getTaskId() { return this.task_id;}

}
