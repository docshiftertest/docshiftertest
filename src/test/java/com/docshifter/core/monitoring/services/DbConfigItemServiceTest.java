package com.docshifter.core.monitoring.services;

import com.docshifter.core.AbstractSpringTest;
import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.entities.DbConfigurationItem;
import com.docshifter.core.monitoring.mappings.DbConfigurationItemConverter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class DbConfigItemServiceTest extends AbstractSpringTest {
    @Autowired
    private DbConfigurationItemService dbConfigurationItemService;

    @Autowired
    private DbConfigurationItemConverter dbConfigurationItemConverter;

    private DbConfigurationItemDto configurationItem;

    @Before
    public void setUp() {
        configurationItem = new DbConfigurationItemDto();
        configurationItem.setDriver("org.hsqldb.jdbc.JDBCDriver");
        configurationItem.setConnection("jdbc:hsqldb:mem:docshifter");
        configurationItem.setDbUser("metrics_system");
        configurationItem.setTableName("test_db_notification");
        configurationItem.setPassword("mb282wu7nvDkbQRkfXvA");
    }

    @Test
    public void shouldInject() {
        assertThat(dbConfigurationItemService).isNotNull();
        assertThat(dbConfigurationItemConverter).isNotNull();
    }

    @Test
    public void shouldConvertConfigToEntity() {
        DbConfigurationItem entity = dbConfigurationItemConverter.convertToEntity(configurationItem);
        assertThat(entity).isNotNull();
        assertThat(entity.getConnection()).isEqualTo(configurationItem.getConnection());
        assertThat(entity.getDriver()).isEqualTo(configurationItem.getDriver());
        assertThat(entity.getDbUser()).isEqualTo(configurationItem.getDbUser());
        assertThat(entity.getTableName()).isEqualTo(configurationItem.getTableName());
    }

    @Test
    public void shouldStoreConfigurationItem() {
        DbConfigurationItemDto result = dbConfigurationItemService.add(11L, configurationItem);
        assertNotNull(result);
       	assertThat(result.getId()).isNotZero();
    }
}
