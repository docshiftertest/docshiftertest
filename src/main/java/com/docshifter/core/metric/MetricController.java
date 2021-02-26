package com.docshifter.core.metric;

import com.docshifter.core.metric.services.MetricManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Boyan Dunev created on 22/02/2021
 */

@RestController
@RequestMapping("metrics/")
public class MetricController {

    private final MetricManagementService metricManagementService;

    @Autowired
    public MetricController(MetricManagementService metricManagementService) {
        this.metricManagementService = metricManagementService;
    }

    @GetMapping("successfulWorkflows")
    public int successfulWfs() {
        return this.metricManagementService.successfulWfs();
    }
//
    @GetMapping("successfulFiles")
    public int successfulFiles() {
        return this.metricManagementService.successfulFiles();
    }

    @GetMapping("allWorkflows")
    public int allWfs(){
        return this.metricManagementService.allWfs();
    }

    @GetMapping("allFiles")
    public int allFiles(){
        return this.metricManagementService.allFiles();
    }
}

