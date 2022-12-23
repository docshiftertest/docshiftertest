package com.docshifter.core.monitoring.services;

import com.docshifter.core.graphAPI.MSGraphAuthenticationBuilder;
import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.utils.TemplateUtils;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UserSendMailParameterSet;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.AttachmentCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by blazejm on 12.05.2017.
 */
@Log4j2
@Service
public class EmailServiceImpl implements EmailService {

    private static final String DEFAULT_TITLE = "Notification";
    private static final String O_DATA_TYPE = "#microsoft.graph.fileAttachment";

    @Override
    public void sendEmail(MailConfigurationItemDto configItem, String toAddress, NotificationDto notification, EventListener listener) {

        try {
            if (checkIfShouldSendUsingOffice365(configItem)) {
                log.debug("Trying to send email notification using Office 365.");
                sendEmailUsingOffice365(configItem, toAddress, notification, listener);
            }
            else if (checkIfCouldSendSmtpEmail(configItem)) {
                log.debug("Trying to send email notification using SMTP protocol.");
                sendEmailUsingSmtp(configItem, toAddress, notification, listener);
            }
            else {
                log.warn("It was not possible to send the email notification. Please check the configurations.");
            }
        }
        catch (Exception ex) {
            log.error(String.format("Unknown exception: %s", ex.getMessage()), ex);
        }
    }

    /**
     * Sends email notification using Office 365
     * @param configItem {@link MailConfigurationItemDto} with all the options to send the email
     * @param toAddress address to send the email
     * @param notification notification to be sent
     * @param listener event listener
     */
    private void sendEmailUsingOffice365(MailConfigurationItemDto configItem, String toAddress, NotificationDto notification, EventListener listener) throws IOException {

        GraphServiceClient<?> graphClient = MSGraphAuthenticationBuilder.createGraphClient(
                configItem.getClientId(),
                configItem.getClientSecret(),
                configItem.getTenant()
        );

        Message message = createHtmlMessageOffice365(configItem, toAddress, notification, listener);

        UserSendMailParameterSet userSendMailParameterSet = new UserSendMailParameterSet();
        userSendMailParameterSet.message = message;
        userSendMailParameterSet.saveToSentItems = true;

        graphClient.users(configItem.getFromAddress()).sendMail(userSendMailParameterSet).buildRequest().post();

        log.debug("Email sent to [{}] with message: [{}] successfully.",
                toAddress, notification.getMessage());
    }

    /**
     * Creates a HTML email to be sent
     * @param configItem {@link MailConfigurationItemDto} with the configurations to send the email
     * @param notification notification to be sent
     * @param listener event listener
     * @return the {@link Message} to be sent using Office 365
     */
    private Message createHtmlMessageOffice365(MailConfigurationItemDto configItem,
                                               String toAddress,
                                               NotificationDto notification,
                                               EventListener listener) throws IOException {

        log.debug("Creating the email message.");

        Recipient recipient = new Recipient();
        recipient.emailAddress = new EmailAddress();
        recipient.emailAddress.address = toAddress;

        List<Recipient> recipientList = new ArrayList<>();
        recipientList.add(recipient);

        ItemBody itemBody = new ItemBody();
        itemBody.content = getEmailBody(configItem, notification, listener);
        itemBody.contentType = BodyType.HTML;

        Message message = new Message();
        message.body = itemBody;
        message.subject = getSubject(configItem, notification, listener);
        message.toRecipients = recipientList;

        handleAttachments(message, notification);

        return message;
    }

    /**
     * Gets the email body to send
     * @param configItem {@link MailConfigurationItemDto} with the configurations to send the email
     * @param notification notification to be sent
     * @param listener event listener
     * @return the email body
     */
    private String getEmailBody(MailConfigurationItemDto configItem, NotificationDto notification, EventListener listener) {

        String emailBody;

        if (configItem.getTemplateBody() != null) {
            emailBody = getTextFromTemplate(configItem.getTemplateBody(), notification, listener);
        }
        else {
            emailBody = notification.getMessage();
        }

        return emailBody;
    }

    /**
     * Send email using the SMTP protocol
     * @param configItem {@link MailConfigurationItemDto} with all the options to send the email
     * @param toAddress address to send the email
     * @param notification notification to be sent
     * @param listener event listener
     * @throws MessagingException exceptiom while trying to send the email
     */
    private void sendEmailUsingSmtp(MailConfigurationItemDto configItem, String toAddress, NotificationDto notification, EventListener listener) throws MessagingException {
        JavaMailSenderImpl mailSender = createMailSender(configItem);
        MimeMessage message = createHtmlMessage(mailSender, configItem, toAddress, notification, listener);
        mailSender.send(message);
        log.info("sendEmail to: " + toAddress
                + " with message: " + notification.getMessage() + " successful");
    }

    /**
     * Checks if it should try to send email using Office 365
     * @param configItem {@link MailConfigurationItemDto} with all the options to send the email
     * @return either if it should use Office 365 or not to send email
     */
    private boolean checkIfShouldSendUsingOffice365(MailConfigurationItemDto configItem) {

        return StringUtils.isNotBlank(configItem.getClientId())
                && StringUtils.isNotBlank(configItem.getClientSecret())
                && StringUtils.isNotBlank(configItem.getTenant());
    }

    /**
     * Checks if it is possible to send SMTP email
     * @param configItem {@link MailConfigurationItemDto} with all the options to send the email
     * @return either if all the configurations to send email using SMTP protocol are filled or not
     */
    private boolean checkIfCouldSendSmtpEmail(MailConfigurationItemDto configItem) {

        return StringUtils.isNotBlank(configItem.getHost())
                && StringUtils.isNotBlank(configItem.getUsername())
                && StringUtils.isNotBlank(configItem.getPassword())
                && configItem.getPort() >= 0;
    }


    /**
     * Handles the attachments in the notification
     * @param message email message to be sent
     * @param notification notification to
     * @throws IOException exception while reading the bytes from a file
     */
    private void handleAttachments(Message message, NotificationDto notification) throws IOException {

        if (notification.getAttachments() != null) {

            log.debug("Adding attachments to the email.");

            List<Attachment> attachmentsList = new ArrayList<>();

            for (File file : notification.getAttachments()) {

                log.debug("Processing the file [{}]", file.getName());

                FileAttachment fileAttachment = new FileAttachment();
                fileAttachment.name = file.getName();
                fileAttachment.contentBytes = Files.readAllBytes(Paths.get(file.getPath()));
                fileAttachment.oDataType = O_DATA_TYPE;

                attachmentsList.add(fileAttachment);
            }

            AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
            attachmentCollectionResponse.value = attachmentsList;
            // passing null here following the Microsoft documentation
            message.attachments =  new AttachmentCollectionPage(attachmentCollectionResponse, null);
        }
    }

    private JavaMailSenderImpl createMailSender(MailConfigurationItemDto configItem) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(configItem.getHost());
        mailSender.setPort(configItem.getPort());
        mailSender.setUsername(configItem.getUsername());
        mailSender.setPassword(configItem.getPassword());
        Properties properties = new Properties();
        if ("smtp.gmail.com".equalsIgnoreCase(configItem.getHost())) {
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.auth", "true");
            mailSender.setPort(587);
            mailSender.setJavaMailProperties(properties);
        }
        else if (configItem.isSsl()) {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtps.ssl.checkserveridentity", "true");
            properties.put("mail.smtps.ssl.trust", "*");
            mailSender.setJavaMailProperties(properties);
        }

        return mailSender;
    }

    private MimeMessage createHtmlMessage(JavaMailSender mailSender,
                                          MailConfigurationItemDto configItem,
                                          String toAddress,
                                          NotificationDto notification,EventListener listener) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
        helper.setFrom(configItem.getFromAddress());
        helper.setTo(toAddress);
        helper.setSubject(getSubject(configItem, notification, listener));
        String emailBody = getEmailBody(configItem, notification,listener);
        helper.setText(emailBody, true);
        if (notification.getAttachments() != null) {
            for (File attachment : notification.getAttachments()) {
                try {
                    log.debug(String.format("Adding attachment: %s", attachment.getName()));
                    helper.addAttachment(attachment.getName(), attachment);
                } catch (Exception ex) {
                    log.error(String.format("Unknown exception: %s", ex.getMessage()), ex);
                    ex.printStackTrace();
                }
            }
        }
        return mimeMessage;
    }

    /**
     * Gets the subject for a {@link MailConfigurationItemDto}
     * @param configItem {@link MailConfigurationItemDto} with all the options to send the email
     * @param notification notification to be sent
     * @param listener event listener
     * @return subject for the email
     */
    private String getSubject(MailConfigurationItemDto configItem, NotificationDto notification, EventListener listener) {

        String emailSubject;

        if (configItem.getTemplateTitle() != null) {
            emailSubject = getTextFromTemplate(configItem.getTemplateTitle(), notification, listener);
        }
        else {
            emailSubject = DEFAULT_TITLE;
        }

        return emailSubject;
    }

    private String getTextFromTemplate(String template, NotificationDto notification,EventListener listener) {
        return TemplateUtils.getTextFromTemplate(template, notification,listener);
    }
}

