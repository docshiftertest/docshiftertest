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
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
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
    private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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
    public DocumentCounterDTO createDocumentCounterDto(String task, long counts){
        return DocumentCounterDTO.builder().task_id(task).counts(counts).build();

    }

    /**
     * @param filename The source filename from the Task object
     * @return int count The number of counted files
     */
    public long countFiles(String filename) {
        long counts=1;
        String extension = FileUtils.getExtension(filename);
        logger.debug("The file extension is:" + extension);

        if(StringUtils.isBlank(extension)) {
            logger.warn("Extension is blank, failure might occur; file will otherwise be counted as 1");
        }

        //TODO: Handle other archive file formats (rar, 7z, tar, etc.)
        //counts all files in an archive
        //Yoda fix - compared string first avoids NullPointerExceptions from extension
        if ("zip".equalsIgnoreCase(extension)) { //
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
        else if ("eml".equalsIgnoreCase(extension) || "msg".equalsIgnoreCase(extension)) {
            MailMessage eml = MailMessage.load(filename);
            counts = eml.getAttachments().size() + 1; // counts is set to all attachments plus the e-mail body
            return counts;
        }

        return counts; //default case

    }

    // Creates the encrypted file containing the counts that should be sent to DocShifter

    /**
     *
     * @param tempPath The console.temp.dir value from the application.properties file; retrieved in the Controller
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
        //TODO: Find a more straightforward and understandable solution
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

        //Creates the unencrypted file
        File countsFile = new File(tempPath + name);
        try (FileWriter writer = new FileWriter(countsFile)) {
            File dir = new File(tempPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // TODO: Never have an unencrypted version of the file stored on the disk
            //  A way to do that would be to encrypt the string and only then write it to a file, but that was running into issues (not actually getting written to the file)

            // Writes the file contents; some padding provided at the end of the lines to make the contents slightly less obvious
            writer.write("Files: " + counts +
                        "   \n" + "Tasks:" + tasks + "     \n"
                        + "At: " + timestamp);
//            writer.write("Total files:         ")
            //TODO: When the metric system is expanded, add other metrics we are interested in
        } catch (IOException e) {
            countsFile.delete(); //Remove plaintext file in case of errors!
            logger.warn(e);
            logger.error("Counts file not created");
        }

        //Encrypts the counts file created above
        try {
            CountsEncryption encryptor = new CountsEncryption();
            // Gets the public key from the file retrieved above
            PublicKey publicKey = encryptor.getPublic(match[0].toString());

            //Replaces the plaintext counts file with the encrypted version
            encryptor.encryptFile(encryptor.getFileInBytes(new File(tempPath+name)),
                    new File(tempPath+name), publicKey);
        } catch  (Exception exception) {
            countsFile.delete(); //Remove plaintext file in case of errors!
            logger.error("Failed to encrypt file");
            exception.printStackTrace();
        }
        // TODO: First send e-mail, then open e-mail client, then point them to encrypted file
        // mailTo link, encrypt the body of the email; include license number? -> DSLicenseCode
    }
}
