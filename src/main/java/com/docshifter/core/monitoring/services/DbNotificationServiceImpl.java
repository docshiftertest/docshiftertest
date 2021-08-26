package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.docshifter.core.monitoring.utils.TemplateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class DbNotificationServiceImpl implements DbNotificationService {

    @Override
    public void sendNotification(DbConfigurationItemDto dbConfigurationItem, NotificationDto notification) throws SQLException {

        String insertTableSQL = String.format("INSERT INTO %s"
                + "(timestamp, level, task_id, message, attachments) VALUES"
                + "(?,?,?,?,?)",
                dbConfigurationItem.getTableName());

        try (Connection dbConnection = getDBConnection(dbConfigurationItem);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(insertTableSQL)) {

            preparedStatement.setTimestamp(1, getCurrentTimeStamp());
            preparedStatement.setString(2, notification.getLevel().toString());
            preparedStatement.setString(3, notification.getTaskId());
            preparedStatement.setString(4, notification.getMessage());
            preparedStatement.setString(5, getAttachmentsString(notification));

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            log.error("SQL insert error", e);
        }
    }

    @Override
    public List<NotificationDto> getNotifications(DbConfigurationItemDto dbConfigurationItem) {
    //Method is only used in the test class

        String selectTableSQL = "SELECT * FROM test_db_notification";
        List<NotificationDto> result = new ArrayList<>();

        try (Connection dbConnection = getDBConnection(dbConfigurationItem);
             PreparedStatement preparedStatement = dbConnection.prepareStatement(selectTableSQL)) {

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                NotificationDto notificationDto = new NotificationDto();
                notificationDto.setLevel(NotificationLevels.valueOf(rs.getString("level")));
                notificationDto.setTaskId(rs.getString("task_id"));
                notificationDto.setMessage(rs.getString("message"));
                notificationDto.setAttachments(new File[] {new File(rs.getString("attachments"))});

                result.add(notificationDto);
            }
        }
        catch (SQLException e) {
            log.error("SQL Select error", e);
        }

        return result;
    }

    private Connection getDBConnection(DbConfigurationItemDto dbConfigurationItem) {
        Connection dbConnection = null;
        try {
            Class.forName(dbConfigurationItem.getDriver());
        }
        catch (ClassNotFoundException cunfy) {
            log.error("Driver error", cunfy);
        }

        try {
            dbConnection = DriverManager.getConnection(
                    dbConfigurationItem.getConnection(),
                    dbConfigurationItem.getDbUser(),
                    dbConfigurationItem.getPassword());
        }
        catch (SQLException squealy) {
            log.error("Connection error", squealy);
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
        return String.join(", ", TemplateUtils.getAttachmentNames(notification));
    }

}
