package com.docshifter.core.metrics.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class ServiceHealthDTO {

    private String status;

    private Map<String, String> dbStatus;

    private Map<String, Object> diskSpace;


}
