package com.docshifter.core.metrics.dtos;

import lombok.Getter;
import lombok.Setter;

/**
Data transfer object to store metrics
 Currently stores filename and counts, will be expanded with other metrics
 Metrics other than the basic counter should be implemented as a licensable module
 */
@Getter
@Setter
public class DocumentCounterDTO {

    private String filename;
    private int counts;

}
