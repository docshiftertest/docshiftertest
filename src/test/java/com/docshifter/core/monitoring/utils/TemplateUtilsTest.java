package com.docshifter.core.monitoring.utils;

import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateUtilsTest {
    private static final Logger log = Logger.getLogger(TemplateUtilsTest.class.getName());

    private String template = "\n{\n"
            + "\t\"name\": \"Error in {{taskId}}\",\n"
            + "\t\"description\": \"Error details, level: {{level}}, message: {{message}}, source file: {{sourceFilePath}}, attachments: {{attachments}}\",\n"
            + "\t\"attachments\": {{attachmentsJson}}\n"
        + "}";


    @Test
    public void shouldGenerateValidJsonFromTemplate() {
        NotificationDto notification = new NotificationDto();
        notification.setTaskId("tsk1");
        notification.setLevel(NotificationLevels.ERROR);
        notification.setAttachments(new File[] {
                new File("processing/file1.doc"),
                new File("processing/file2.docx")
        });
        notification.setSourceFilePath("C:\\Users\\SomeUser\\Documents\\asourcefile.doc");
        notification.setMessage("Processing failed");
        log.debug(template);
        String text = TemplateUtils.getTextFromTemplate(template, notification,null);
        log.debug(text);
        assertThat(text).isNotEmpty();
        assertThat(isJsonValid(text)).isTrue();
    }

    private static boolean isJsonValid(String jsonString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonString);
            return true;
        } catch (IOException ioe) {
            log.error("Json validation", ioe);
            return false;
        }
    }
}
