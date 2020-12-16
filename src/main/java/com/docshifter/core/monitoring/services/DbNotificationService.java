package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;

import java.sql.SQLException;

/**
 * Created by blazejm on 29.09.2017.
 */
public interface DbNotificationService {
    void sendNotification(DbConfigurationItemDto dbConfigurationItem, NotificationDto notification) throws SQLException;
}
