package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.dtos.SnmpConfigurationItemDto;

/**
 * Created by blazejm on 18.05.2017.
 */
public interface SnmpNotificationService {
    void sendNotification(SnmpConfigurationItemDto snmpConfigurationItem, NotificationDto notification);
}
