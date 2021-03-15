package com.docshifter.core.metrics;

import com.docshifter.core.metrics.repositories.DocumentCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Boyan Dunev created on 22/02/2021
 */

@RestController
@RequestMapping("metrics/")
public class DocumentCounterController {

    @Autowired
    DocumentCounterRepository counterRepository;

    @Autowired
    public DocumentCounterController() {
    }
    @GetMapping("successfulFiles")
    public int successfulFiles() {
        return this.counterRepository.selectTotalCounts();
    }
}

