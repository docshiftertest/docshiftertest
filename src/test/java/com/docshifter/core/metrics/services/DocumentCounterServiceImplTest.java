package com.docshifter.core.metrics.services;

import com.docshifter.core.AbstractSpringTest;
import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.entities.DocumentCounter;
import com.docshifter.core.metrics.repositories.DocumentCounterRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentCounterServiceImplTest extends AbstractSpringTest {
    @Autowired
    private DocumentCounterServiceImpl counterService;

    @Autowired
    private DocumentCounterRepository counterRepository;

    @Test
    public void shouldCreateDto(){
        String id = "sometask";
        DocumentCounterDTO metric = counterService.createDocumentCounterDto(id, 1);

        assertThat(metric.getTask_id()).isEqualTo(id);
        assertThat(metric.getCounts()).isEqualTo(1);
    }

    @Test
    public void shouldSaveEntity() {
        DocumentCounterDTO counter = new DocumentCounterDTO().builder().task_id("sometask").counts(1).build();
        DocumentCounter entity = counterService.saveDocumentCounter(counter);
//        System.out.println("Written)");

        assertThat(counterRepository.findById("sometask")).isNotNull();
    }
}
