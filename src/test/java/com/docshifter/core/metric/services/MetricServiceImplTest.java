package com.docshifter.core.metric.services;

import com.docshifter.core.metric.MetricDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MetricServiceImplTest {
    @Autowired
    private MetricServiceImpl metricService;

    @Test
    public void shouldCreateDto(){
        String filename = "filename";
        MetricDto metric = metricService.createMetricDto(filename);

        assertThat(metric.getFilename()).isEqualTo(filename);
    }
}