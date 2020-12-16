package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.dtos.WebhookConfigurationItemDto;

/**
 * Created by blazejm on 16.05.2017.
 */
public interface WebhookNotificationService {
    void sendNotification(WebhookConfigurationItemDto webhookConfigItem, NotificationDto notification);
}
