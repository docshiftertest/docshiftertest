package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by blazejm on 29.09.2017.
 */
public interface DbNotificationService {
    void sendNotification(DbConfigurationItemDto dbConfigurationItem, NotificationDto notification) throws SQLException;
    List<NotificationDto> getNotifications(DbConfigurationItemDto dbConfigurationItem);
}
