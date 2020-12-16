package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.services.DbNotificationService;
import com.docshifter.core.monitoring.utils.TemplateUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.stream.Collectors;

@Service
public class DbNotificationServiceImpl implements DbNotificationService {
    private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.DbNotificationServiceImpl.class.getName());

    @Override
    public void sendNotification(DbConfigurationItemDto dbConfigurationItem, NotificationDto notification) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        String insertTableSQL = String.format("INSERT INTO %s"
                + "(timestamp, level, task_id, message, attachments) VALUES"
                + "(?,?,?,?,?)",
                dbConfigurationItem.getTableName());

        try {
            dbConnection = getDBConnection(dbConfigurationItem);
            preparedStatement = dbConnection.prepareStatement(insertTableSQL);

            preparedStatement.setTimestamp(1, getCurrentTimeStamp());
            preparedStatement.setString(2, notification.getLevel().toString());
            preparedStatement.setString(3, notification.getTaskId());
            preparedStatement.setString(4, notification.getMessage());
            preparedStatement.setString(5, getAttachmentsString(notification));

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL insert error", e);
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        }
    }

    private Connection getDBConnection(DbConfigurationItemDto dbConfigurationItem) {
        Connection dbConnection = null;
        try {
            Class.forName(dbConfigurationItem.getDriver());
        } catch (ClassNotFoundException e) {
            log.error("Driver error", e);
        }

        try {
            dbConnection = DriverManager.getConnection(
                    dbConfigurationItem.getConnection(),
                    dbConfigurationItem.getUser(),
                    dbConfigurationItem.getPassword());
            return dbConnection;
        } catch (SQLException e) {
            log.error("Connection error", e);
        }
        return dbConnection;
    }

    private Timestamp getCurrentTimeStamp() {
        java.util.Date now = new java.util.Date();
        return new Timestamp(now.getTime());
    }

    private String getAttachmentsString(NotificationDto notification) {
        if (notification.getAttachments() == null) {
            return null;
        }
        return TemplateUtils.getAttachmentNames(notification)
                .stream()
                .collect(Collectors.joining(", "));
    }

}
