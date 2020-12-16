package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.services.EmailService;
import com.docshifter.core.monitoring.services.EventListener;
import com.docshifter.core.monitoring.utils.TemplateUtils;
import org.apache.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

/**
 * Created by blazejm on 12.05.2017.
 */
@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.EmailServiceImpl.class.getName());
    private static final String DEFAULT_TITLE = "Notification";

    @Override
    public void sendEmail(MailConfigurationItemDto configItem, String toAddress, NotificationDto notification,EventListener listener) {
        try {
            JavaMailSenderImpl mailSender = createMailSender(configItem);
            MimeMessage message = createHtmlMessage(mailSender, configItem, toAddress, notification,listener);
            mailSender.send(message);
            log.info("sendEmail to: " + toAddress
                    + " with message: " + notification.getMessage() + " successful");
        } catch (Exception ex) {
            log.error(String.format("Unknown exception: %s", ex.getMessage()), ex);
            ex.printStackTrace();
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
        helper.setSubject(configItem.getTemplateTitle() != null
                ? getTextFromTemplate(configItem.getTemplateTitle(), notification,listener)
                : DEFAULT_TITLE);
        String emailBody = configItem.getTemplateBody() != null
                ? getTextFromTemplate(configItem.getTemplateBody(), notification,listener)
                : notification.getMessage();
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

    private String getTextFromTemplate(String template, NotificationDto notification,EventListener listener) {
        return TemplateUtils.getTextFromTemplate(template, notification,listener);
    }
}

