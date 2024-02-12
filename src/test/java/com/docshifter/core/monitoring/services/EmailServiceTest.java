package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by blazejm on 29.05.2017.
 */
public class EmailServiceTest extends AbstractServiceTest {
    @Autowired
    private EmailService emailService;

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(new ServerSetup[]{new ServerSetup(0, null, ServerSetup.PROTOCOL_SMTP), new ServerSetup(0, null, ServerSetup.PROTOCOL_IMAP)})
            .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    private ClassLoader classLoader;

    @BeforeEach
    public void beforeTest() {
        super.beforeTest();
        classLoader = getClass().getClassLoader();
        //greenMail = new GreenMail(ServerSetupTest.ALL);
        greenMail.setUser("blaze@localhost", "blaze@localhost", "secret");
        Session smtpSession = greenMail.getSmtp().createSession();

        mailConfigurationItem.setHost(smtpSession.getProperty("mail.smtp.host"));
        mailConfigurationItem.setPort(Integer.parseInt(smtpSession.getProperty("mail.smtp.port")));
        mailConfigurationItem.setFromAddress("blaze@localhost");
        mailConfigurationItem.setSsl(false);
        mailConfigurationItem.setPassword("secret");
    }

    @AfterEach
    public void afterTest() {
        greenMail.stop();
    }

    @Test
    public void shouldInjectService() {
        assertThat(emailService).isNotNull();
    }

    @Test
    public void shouldSendSimpleEmail() throws Exception {
        NotificationDto notification = new NotificationDto();
        notification.setLevel(NotificationLevels.ERROR);
        notification.setMessage("some body");

        emailService.sendEmail(mailConfigurationItem, "blazej.maciaszek@sienn.pl", notification,null);
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        String content = getHtmlContent(receivedMessage);
        // TODO: Uncomment once org.apache.commons.commons-email 2.0 is available and bumped in the POM!
        //assertThat(content).isEqualToIgnoringWhitespace("some body");
    }

    @Test
    public void shouldSendSimpleEmailNoAuth() throws Exception {
        NotificationDto notification = new NotificationDto();
        notification.setLevel(NotificationLevels.ERROR);
        notification.setMessage("some body");

        mailConfigurationItem.setUsername("");
        mailConfigurationItem.setPassword("");
        emailService.sendEmail(mailConfigurationItem, "blazej.maciaszek@sienn.pl", notification,null);
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        String content = getHtmlContent(receivedMessage);
        // TODO: Uncomment once org.apache.commons.commons-email 2.0 is available and bumped in the POM!
        //assertThat(content).isEqualToIgnoringWhitespace("some body");
    }

    @Test
    public void shouldSendEmailWithTemplate() throws Exception {
        NotificationDto notification = new NotificationDto();
        notification.setLevel(NotificationLevels.ERROR);
        notification.setMessage("some body");

        mailConfigurationItem.setTemplateBody("Body: {{message}}, level: {{level}}");
        mailConfigurationItem.setTemplateTitle("custom title");

        emailService.sendEmail(mailConfigurationItem, "blazej.maciaszek@sienn.pl", notification,null);

        assertThat(greenMail.getReceivedMessages()[0].getSubject()).isEqualTo("custom title");

        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        String content = getHtmlContent(receivedMessage);
        // TODO: Uncomment once org.apache.commons.commons-email 2.0 is available and bumped in the POM!
        //assertThat(content).isEqualToIgnoringWhitespace("Body: some body, level: ERROR");
    }

    @Test
    public void shouldSendEmailWithAttachment() throws Exception {
        File attachment = getTestFile();

        NotificationDto notification = new NotificationDto();
        notification.setLevel(NotificationLevels.ERROR);
        notification.setMessage("some body");
        notification.setAttachments(new File[] {attachment});

        emailService.sendEmail(mailConfigurationItem, "blazej.maciaszek@sienn.pl", notification,null);
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];

        Multipart multipart = (Multipart) receivedMessage.getContent();
        assertThat(multipart).isNotNull();
        List<InputStream> attachments = getAttachments(multipart);
        assertThat(attachments).isNotEmpty();
    }

    private File getTestFile() throws URISyntaxException {
        return Paths.get(classLoader.getResource("attachment.txt").toURI()).toFile();
    }

    private List<InputStream> getAttachments(Multipart multipart) throws Exception {
        List<InputStream> result = new ArrayList<>();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                result.add(bodyPart.getInputStream());
            }
        }
        return result;
    }

    private String getHtmlContent(MimeMessage mimeMessage) throws Exception {
        return null;
        // TODO: Uncomment once org.apache.commons.commons-email 2.0 is available and bumped in the POM!
//        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
//        parser.parse();
//        return parser.getHtmlContent();
    }
}
