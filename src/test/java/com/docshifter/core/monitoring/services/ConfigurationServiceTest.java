package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.ConfigurationDto;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by blazejm on 30.05.2017.
 */
public class ConfigurationServiceTest extends AbstractServiceTest {


    @Test
    public void shouldInjectService() {
        assertThat(configurationService).isNotNull();
    }

    @Test
    public void shouldSaveConfiguration() {
        ConfigurationDto result = configurationService.addConfiguration(sampleConfiguration1);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
    }

    @Test
    public void shouldFetchConfigurations() {
        saveConfiguration();

        List<ConfigurationDto> result = configurationService.getAll();
        assertThat(result).isNotEmpty();
        ConfigurationDto firstResult = result.get(0);
        assertThat(firstResult.getConfigurationItems().size())
                .isEqualTo(sampleConfiguration1.getConfigurationItems().size());
    }

    @Test
    public void shouldFetchConfigurationById() {
        saveConfiguration();

        ConfigurationDto resultById = configurationService.getById(sampleConfiguration1.getId());
        assertNotNull(resultById);
        assertThat(resultById.getId()).isEqualTo(sampleConfiguration1.getId());
    }

    @Test
    public void shouldUpdateConfiguration() {
        saveConfiguration();

        sampleConfiguration1.setName("new name");
        ConfigurationDto resultById = configurationService.updateConfiguration(sampleConfiguration1.getId(), sampleConfiguration1);
        assertNotNull(resultById);
        assertThat(resultById.getName()).isEqualTo("new name");
    }

    @Test
    public void shouldDeleteConfiguration() {
        saveConfiguration();

        configurationService.deleteConfiguration(sampleConfiguration1.getId());
        ConfigurationDto resultById = configurationService.getById(sampleConfiguration1.getId());
        assertNull(resultById);
    }
}
