package com.docshifter.core.metrics.services;

import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.entities.DocumentCounter;

public interface DocumentCounterService {
    DocumentCounter saveDocumentCounter(DocumentCounterDTO dto);

    DocumentCounterDTO createDocumentCounterDto(String task, long counts);

    long countFiles(String filename);

    void exportCounts(String path, String keyPath);
}
