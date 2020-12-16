package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;

import java.io.File;

/**
 * Created by blazejm on 12.05.2017.
 */
public interface NotificationService {
    void sendNotification(long configurationId, NotificationLevels level, String taskId, String message);

    void sendNotification(long configurationId, NotificationLevels level, String taskId, String message, String sourceFilePath);

    void sendNotification(long configurationId, NotificationLevels level, String taskId, String message, File... attachments);

    void sendNotification(long configurationId, NotificationLevels level, String taskId, String message, String sourceFilePath, File... attachments);

    void sendNotification(long configurationId, NotificationDto notification);
}
