package com.docshifter.core.metrics.services;

import com.aspose.email.Attachment;
import com.aspose.email.MailMessage;
import com.aspose.email.SaveOptions;
import com.docshifter.core.AbstractSpringTest;
import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailCountTest extends AbstractSpringTest {

    @Autowired
    private DocumentCounterServiceImpl counterService;

    MailMessage message;

    @Before
    public void setUp() {
        message = new MailMessage("testing@docshifter.local", "development@docshifter.local",
                "Subject",
                "Here is your mail message number");
    }

    @Test
//    @Ignore
    public void shouldCountEmail(){
        message.save("target/test-classes/TestMessage.eml", SaveOptions.getDefaultEml());
        String filename = "target/test-classes/TestMessage.eml";
        String task_id = "sometask";
        int counts = counterService.countFiles(filename);
        DocumentCounterDTO metric = counterService.createDocumentCounterDto(task_id, counts);

        assertThat(metric.getCounts()).isEqualTo(1);

    }

    @Test
//    @Ignore
    public void shouldCountAttachments() {
        Attachment attachment = new Attachment("target/test-classes/attachment.txt");
        message.addAttachment(attachment);
        message.save("target/test-classes/TestMessage.eml", SaveOptions.getDefaultEml());
        System.out.println(message.getAttachments().size());

        String filename = "target/test-classes/TestMessage.eml";
        String task_id = "sometask";
        int counts = counterService.countFiles(filename);
        DocumentCounterDTO metric = counterService.createDocumentCounterDto(task_id, counts);

        assertThat(metric.getCounts()).isEqualTo(2);
    }
}
