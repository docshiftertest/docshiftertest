package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.AbstractSpringTest;
import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.entities.TestDbNotification;
import com.docshifter.core.monitoring.entities.TestDbNotificationRepository;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.docshifter.core.monitoring.services.DbNotificationService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class DbNotificationServiceTest extends AbstractSpringTest {
    @Autowired
    private DbNotificationService dbNotificationService;

    @Autowired
    private TestDbNotificationRepository testDbNotificationRepository;

    private DbConfigurationItemDto configurationItem;

    @Before
    public void setUp() {
        configurationItem = new DbConfigurationItemDto();
        configurationItem.setDriver("org.hsqldb.jdbc.JDBCDriver");
        configurationItem.setConnection("jdbc:hsqldb:mem:docshifter");
        configurationItem.setUser("sa");
        configurationItem.setTableName("test_db_notification");
        configurationItem.setPassword("sa");
    }

    @Test
    public void shouldInject() {
        assertThat(dbNotificationService).isNotNull();
        assertThat(testDbNotificationRepository).isNotNull();
    }

    @Test
    public void shouldStoreNotificationInDb() throws SQLException {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setLevel(NotificationLevels.ERROR);
        notificationDto.setTaskId("test task");
        notificationDto.setMessage("test message");
        notificationDto.setAttachments(new File[] {new File("test.doc")});

        dbNotificationService.sendNotification(configurationItem, notificationDto);

        Iterable<TestDbNotification> dbNotifications = testDbNotificationRepository.findAll();
        Assertions.assertThat(dbNotifications).isNotNull();
        Assertions.assertThat(dbNotifications).size().isEqualTo(1);

        TestDbNotification dbNotification = dbNotifications.iterator().next();
        assertThat(dbNotification).isNotNull();
        assertThat(dbNotification.getLevel()).isEqualTo("ERROR");
        assertThat(dbNotification.getTaskId()).isEqualTo("test task");
        assertThat(dbNotification.getMessage()).isEqualTo("test message");
        assertThat(dbNotification.getAttachments()).isEqualTo("test.doc");
    }

}
