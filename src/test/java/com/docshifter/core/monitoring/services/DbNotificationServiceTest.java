package com.docshifter.core.monitoring.services;

import com.docshifter.core.AbstractSpringTest;
import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DbNotificationServiceTest extends AbstractSpringTest {
    @Autowired
    private DbNotificationService dbNotificationService;

    private DbConfigurationItemDto configurationItem;

   @Before
    public void setUp() {
       //TODO: Update when the metrics user and pass are finalized
       configurationItem = new DbConfigurationItemDto();
       configurationItem.setDriver("org.hsqldb.jdbc.JDBCDriver");
       configurationItem.setConnection("jdbc:hsqldb:mem:docshifter");
       configurationItem.setUser("metrics_system");
       configurationItem.setTableName("test_db_notification");
       configurationItem.setPassword("mb282wu7nvDkbQRkfXvA");
    }

    @Test
    public void shouldInject() {
        assertThat(dbNotificationService).isNotNull();
    }

    @Test
    public void shouldStoreNotificationInDb() throws SQLException {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setLevel(NotificationLevels.ERROR);
        notificationDto.setTaskId("test task");
        notificationDto.setMessage("test message");
        notificationDto.setAttachments(new File[] {new File("test.doc")});

        dbNotificationService.sendNotification(configurationItem, notificationDto);
        List<NotificationDto> dbNotifications = dbNotificationService.getNotifications(configurationItem);
        Assertions.assertThat(dbNotifications).isNotNull();
        Assertions.assertThat(dbNotifications).size().isEqualTo(1);

        NotificationDto dbNotification = dbNotifications.get(0);
        assertThat(dbNotification).isNotNull();
        assertThat(dbNotification.getLevel().toString()).isEqualTo("ERROR");
        assertThat(dbNotification.getTaskId()).isEqualTo("test task");
        assertThat(dbNotification.getMessage()).isEqualTo("test message");
        assertThat(Arrays.stream(dbNotification.getAttachments()).findFirst().get().getName()).isEqualTo("test.doc");
    }

}
