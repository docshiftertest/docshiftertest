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
    private int counts;

}
