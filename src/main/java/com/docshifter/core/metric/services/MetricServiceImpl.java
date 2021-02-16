package com.docshifter.core.metric.services;

import com.aspose.email.MailMessage;
import com.docshifter.core.metric.MetricDto;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.zip.ZipFile;

@Service
public class MetricServiceImpl implements MetricService{
    private static final Logger logger = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

    public void storeMetric() {}

    public MetricDto createMetricDto(String filename){
        MetricDto metric = new MetricDto();
        metric.setFilename(filename);

        metric.setCounts(countFiles(filename));

        logger.info("Metric input filename: " + metric.getFilename());
        logger.info("Metric counts: " + metric.getCounts());

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
