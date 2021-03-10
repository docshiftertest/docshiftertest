package com.docshifter.core.metrics.services;

import com.aspose.email.MailMessage;
import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * Handles operations related to metrics.
 *
 * For now handles counting the incoming files and storing the counts.
 */
@Service
public class MetricServiceImpl implements MetricService{
    private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

    public void storeMetric(int count) {
        try {
            File myObj = new File("E:/DocShifter/Repository/docshifter-core/docshifter-management/src/assets/metrics.txt");
            if (myObj.createNewFile()) {
                logger.info("File created: " + myObj.getName());
            } else {
                logger.info("File already exists.");
            }
        } catch (IOException e) {
            logger.info("An error occurred.");
            logger.error(e);
        }

        try {
            FileWriter myWriter = new FileWriter("E:/DocShifter/Repository/docshifter-core/docshifter-management/src/assets/metrics.txt", true);
            myWriter.write(count + ",");
            myWriter.close();
            logger.info("Successfully wrote to the file, yay 2!");
        } catch (IOException e) {
            logger.info("An error occurred.");
            logger.error(e);
        }
    }

    public DocumentCounterDTO createMetricDto(String filename){
        DocumentCounterDTO metric = new DocumentCounterDTO();
        metric.setFilename(filename);

        metric.setCounts(countFiles(filename));

        logger.info("Metric input filename: " + metric.getFilename());
        logger.info("Metric counts: " + metric.getCounts());

        storeMetric(metric.getCounts());

        return metric;
    }

    private int countFiles(String filename) {
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
}
