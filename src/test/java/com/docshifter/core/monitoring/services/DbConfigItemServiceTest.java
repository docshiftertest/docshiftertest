package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.AbstractSpringTest;
import com.docshifter.core.monitoring.dtos.DbConfigurationItemDto;
import com.docshifter.core.monitoring.entities.DbConfigurationItem;
import com.docshifter.core.monitoring.mappings.DbConfigurationItemConverter;
import com.docshifter.core.monitoring.services.DbConfigurationItemService;
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
        configurationItem.setConnection("jdbc:hsqldb:mem:unittests");
        configurationItem.setUser("sa");
        configurationItem.setTableName("test_db_notification");
        configurationItem.setPassword("randomly");
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
        assertThat(entity.getUser()).isEqualTo(configurationItem.getUser());
        assertThat(entity.getTableName()).isEqualTo(configurationItem.getTableName());
    }

    @Test
    public void shouldStoreConfigurationItem() {
        DbConfigurationItemDto result = dbConfigurationItemService.add(11L, configurationItem);
        assertNotNull(result);
       	assertThat(result.getId()).isNotZero();
    }
}