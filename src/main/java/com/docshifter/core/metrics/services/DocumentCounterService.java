package com.docshifter.core.metrics.services;

import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.entities.DocumentCounter;
import org.springframework.core.io.Resource;

public interface DocumentCounterService {

    DocumentCounter saveDocumentCounter(DocumentCounterDTO dto);

    DocumentCounterDTO createDocumentCounterDto(String task, long counts);

    long countFiles(String filename);

    Resource exportCounts(String path);

    DocumentCounterDTO getTotals();
}
