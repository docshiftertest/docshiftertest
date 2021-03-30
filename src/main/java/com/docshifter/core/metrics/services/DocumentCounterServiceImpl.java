package com.docshifter.core.metrics.services;

import com.aspose.email.MailMessage;
import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.entities.DocumentCounter;
import com.docshifter.core.metrics.repositories.DocumentCounterRepository;
import com.docshifter.core.utils.FileUtils;
import com.docshifter.core.security.metrics.CountsEncryption;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipFile;

/**
 * Handles operations related to metrics.
 *
 * For now handles counting the incoming files and storing the counts.
 */
@Service
public class DocumentCounterServiceImpl implements DocumentCounterService {
    private static final Logger logger = Logger.getLogger(new Object() {
    }.getClass().getEnclosingClass());

    @Autowired
    DocumentCounterRepository documentCounterRepository;

    public DocumentCounter saveDocumentCounter(DocumentCounterDTO dto) {
        // convert from Dto to Entity and save
        DocumentCounter entity = new DocumentCounter();
        entity.setTaskId(dto.getTaskId());
        entity.setCounts(dto.getCounts());

        return documentCounterRepository.save(entity);
    }

    //Uses the @Builder annotation to create the DTO
    public DocumentCounterDTO createDocumentCounterDto(String task, long counts) {
        return DocumentCounterDTO.builder().task_id(task).counts(counts).build();

    }

    /**
     * @param filename The source filename from the Task object
     * @return int count The number of counted files
     */
    public long countFiles(String filename) {
        long counts = 1;
        String extension = FileUtils.getExtension(filename);
        logger.debug("The file extension is:" + extension);

        if (StringUtils.isBlank(extension)) {
            logger.warn("Extension is blank, failure might occur; file will otherwise be counted as 1");
        }

        //TODO: Handle other archive file formats (rar, 7z, tar, etc.)
        //counts all files in an archive
        //Yoda fix - compared string first avoids NullPointerExceptions from extension
        if ("zip".equalsIgnoreCase(extension)) { //
            try (ZipFile zf = new ZipFile(filename)) {
                counts = zf.size();
                return counts;
            } catch (IOException e) {
                logger.error("Error with .zip file");
                logger.error(e);
            }
        }
        //counts all attachments and main body in an email
        else if ("eml".equalsIgnoreCase(extension) || "msg".equalsIgnoreCase(extension)) {
            MailMessage eml = MailMessage.load(filename);
            counts = eml.getAttachments().size() + 1; // counts is set to all attachments plus the e-mail body
            return counts;
        }

        return counts; //default case

    }

    // Creates the encrypted file containing the counts that should be sent to DocShifter

    /**
     * @param tempPath   The console.temp.dir value from the application.properties file; retrieved in the Controller
     * @param exportPath Similar to above, the export.dir value
     */
    public void exportCounts(String tempPath, String exportPath) {
        logger.info("Creating counts export file");

        //Retrieving counts from the database
        long counts = documentCounterRepository.selectTotalCounts();
        logger.info("Total counts to date: " + counts);
        long tasks = documentCounterRepository.selectSuccessfulWorkflows();
        logger.info("Total tasks to date: " + tasks);

        //Searches through the DocShifter install directory to find the public key
        //which should be hidden under a non-indicative name
        //Apparently overwrites the 'accept' method in FilenameFilter to find the file we want
        File exportDir = new File(exportPath);
        File[] match = exportDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("publicKey");
            }
        });

        //Prepares the name of the export file, including timestamp
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        String timestamp = dateFormat.format(date); //timestamp as a variable to be included in file name and content
        String name = tempPath + "/" + "counts_" + timestamp;

        // Creates the encrypted file:
        File countsFile = new File(name);
        try {
            File dir = new File(tempPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Writes the string that will be encrypted
            // limited to (key length in bits)/8 -11) bytes
            // so for a 2048-bit RSA key, maximum size of the string is 245 bytes/characters
            // Current size is 60-70 even with maximum values for the longs, so we're good
            String padding = "Files: " + counts +
                    "   \n" + "Tasks:" + tasks + "     \n"
                    + "At: " + timestamp;
            //TODO: When the metric system is expanded, add other metrics we are interested in

            CountsEncryption encryptor = new CountsEncryption();
            // Gets the public key from the file retrieved above
            PublicKey publicKey = encryptor.getPublic(match[0].toString());

            //Writes the encrypted string to a new file
            encryptor.encryptFile(padding.getBytes(StandardCharsets.UTF_8),
                    countsFile, publicKey);
        } catch (Exception exception) {
            countsFile.delete(); //Remove plaintext file in case of errors!
            logger.error("Failed to encrypt file");
            exception.printStackTrace();

            // TODO: First send e-mail, then open e-mail client, then point them to encrypted file
            // mailTo link, encrypt the body of the email; include license number? -> DSLicenseCode
        }
    }
}
