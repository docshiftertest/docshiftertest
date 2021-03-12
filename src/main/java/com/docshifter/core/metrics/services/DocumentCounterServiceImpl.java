package com.docshifter.core.metrics.services;

import com.aspose.email.MailMessage;
import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.entities.DocumentCounter;
import com.docshifter.core.metrics.repositories.DocumentCounterRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.zip.ZipFile;

/**
 * Handles operations related to metrics.
 *
 * For now handles counting the incoming files and storing the counts.
 */
@Service
public class DocumentCounterServiceImpl implements DocumentCounterService {
    private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

    @Autowired
    DocumentCounterRepository documentCounterRepository;

    public DocumentCounter saveDocumentCounter(DocumentCounterDTO dto) {
        // convert from Dto to Entity and save
        DocumentCounter entity = new DocumentCounter();
        entity.setTask_id(dto.getTask_id());
        entity.setCounts(BigInteger.valueOf(dto.getCounts()));

        return documentCounterRepository.save(entity);
    }

    public DocumentCounterDTO createDocumentCounterDto(String task, int counts){
        return DocumentCounterDTO.builder().task_id(task).counts(counts).build();

    }

    public int countFiles(String filename) {
        int counts=1;

        if (filename.endsWith("zip")) {
            try (ZipFile zf = new ZipFile(filename)) {
                counts = zf.size(); // counts is set to all files in the zip file
                return counts;
            }
            catch (IOException e) {
                logger.warn("Error with .zip file");
            }
        }
        else if (filename.endsWith("eml")) {
            MailMessage eml = MailMessage.load(filename);
            counts = eml.getAttachments().size() + 1; // counts is set to all attachments plus the e-mail
            return counts;
        }

            return counts;

    }


//    @PersistenceContext(unitName="metricsEntityManagerFactory")
//    protected EntityManager entityManager;
//
//    @Transactional("metricsTransactionManager")
//    public void insertCounts(DocumentCounter counter){
//        entityManager.createNativeQuery("INSERT INTO METRICS.DOCUMENT_COUNTER VALUES(?, ?)")
//                .setParameter(1, counter.getTask_id())
//                .setParameter(2, counter.getCounts())
//                .executeUpdate();
//    }
//}
}
