package com.docshifter.core.metrics.services;

import com.aspose.email.MailMessage;
import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.entities.DocumentCounter;
import com.docshifter.core.metrics.repositories.DocumentCounterRepository;
import com.docshifter.core.utils.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    //Uses the @Builder annotation to create the DTO
    public DocumentCounterDTO createDocumentCounterDto(String task, int counts){
        return DocumentCounterDTO.builder().task_id(task).counts(counts).build();

    }

    /**
     *
     * @param filename The source filename from the Task object
     * @return int count The number of counted files
     */
    public int countFiles(String filename) {
        int counts=1;
        //toLowerCase uses 'default locale', potential problems related to the culture/locale problem encountered before?
        String extension = FileUtils.getExtension(filename).toLowerCase();
        logger.debug("The file extension is:" + extension);

        //TODO: Handle other archive file formats (rar, 7z, tar, etc.)
        //counts all files in an archive
        if (extension.equals("zip")) {
            try (ZipFile zf = new ZipFile(filename)) {
                counts = zf.size();
                return counts;
            }
            catch (IOException e) {
                logger.error("Error with .zip file");
                logger.error(e);
            }
        }
        //counts all attachments and main body in an email
        else if (extension.equals("eml") || extension.equals("msg")) {
            MailMessage eml = MailMessage.load(filename);
            counts = eml.getAttachments().size() + 1; // counts is set to all attachments plus the e-mail body
            return counts;
        }

        //TODO: Some kind of error handling for unexpected extension types?
        // or would unexpected types crash the process anyway?
            return counts; //default case

    }
}
