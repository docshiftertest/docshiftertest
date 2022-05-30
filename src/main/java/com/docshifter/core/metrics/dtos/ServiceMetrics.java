package com.docshifter.core.metrics.dtos;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class ServiceMetrics implements Serializable {

    private String name;
    private String description;
    private String value;
    private String baseUnit;


}
