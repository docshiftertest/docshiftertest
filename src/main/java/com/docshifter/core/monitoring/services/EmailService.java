package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.MailConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.services.EventListener;

/**
 * Created by blazejm on 12.05.2017.
 */
public interface EmailService {
    void sendEmail(MailConfigurationItemDto configItem, String toAddress, NotificationDto notification, EventListener listener);
}
