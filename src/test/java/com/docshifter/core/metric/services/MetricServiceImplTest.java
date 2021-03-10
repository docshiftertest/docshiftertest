package com.docshifter.core.metric.services;

import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.services.MetricServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MetricServiceImplTest {
    @Autowired
    private MetricServiceImpl metricService;

    @Test
    public void shouldCreateDto(){
        String filename = "filename";
        DocumentCounterDTO metric = metricService.createMetricDto(filename);

        assertThat(metric.getFilename()).isEqualTo(filename);
    }
}
