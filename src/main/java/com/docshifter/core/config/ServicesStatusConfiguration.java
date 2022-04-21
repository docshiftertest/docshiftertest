package com.docshifter.core.config;

import com.docshifter.core.config.services.DiagnosticsService;
import com.docshifter.core.metrics.dtos.ServiceHealthDTO;
import com.docshifter.core.metrics.dtos.ServiceMetrics;
import com.docshifter.core.utils.NetworkUtils;
import com.docshifter.core.work.WorkFolderManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.management.HeapDumpWebEndpoint;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@Log4j2
@Component
public class ServicesStatusConfiguration {

    private final HealthEndpoint healthEndpoint;
    private final MetricsEndpoint metricsEndpoint;
    private final InfoEndpoint infoEndpoint;

    private final DiagnosticsService diagnosticsService;

    private static final List<String> SERVER_DATA_LIST =
            Arrays.asList("disk.free", "disk.total", "jvm.memory.max", "jvm.memory.used",
                    "system.cpu.usage", "jvm.memory.committed");

    public ServicesStatusConfiguration(DiagnosticsService diagnosticsService, HealthEndpoint healthEndpoint, MetricsEndpoint metricsEndpoint, EnvironmentEndpoint environmentEndpoint, InfoEndpoint infoEndpoint, BeansEndpoint beansEndpoint, WorkFolderManager workFolderManager, ConditionsReportEndpoint conditionsReportEndpoint, ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint, HeapDumpWebEndpoint heapDumpWebEndpoint, LoggersEndpoint loggersEndpoint, ThreadDumpEndpoint threadDumpEndpoint) {
        this.healthEndpoint = healthEndpoint;
        this.metricsEndpoint = metricsEndpoint;
        this.infoEndpoint = infoEndpoint;
        this.diagnosticsService = diagnosticsService;
    }

    @JmsListener(destination = Constants.STATUS_QUEUE, containerFactory = Constants.TOPIC_LISTENER)
    public void serviceStatus(ActiveMQMessage message) {
        List<Object> serviceMetricsList = new ArrayList<>();

        SystemHealth healthComponent = (SystemHealth) this.healthEndpoint.health();
        Map<String, String> dbMap = new HashMap<>();

        Map<String, Object> metricsMap = (Map<String, Object>) this.infoEndpoint.info().get("build");
        String serviceName = metricsMap.get("name").toString();

        for (Map.Entry<String, HealthComponent> healthComponentsMap : healthComponent.getComponents().entrySet()) {
            if (healthComponentsMap.getKey().equals("db")) {
                ((CompositeHealth) ((Map.Entry) healthComponentsMap).getValue()).getComponents().forEach(
                        (key, value1) -> dbMap.put(key, value1.getStatus().getCode()));
            }
        }

        //health info
        Map<String, Object> healthMap = new HashMap<>();
        ServiceHealthDTO healthDTO = ServiceHealthDTO.builder().
                status(healthComponent.getStatus().toString()).
                dbStatus(dbMap).
                build();

        try {
            writeJsonFile(healthDTO, message.getBody(String.class) + "\\" + "db.json");
        } catch (JMSException e) {
            log.error("An exception occurred when trying to get the message body");
        }

        //memory info

        try {
            writeJsonFile(this.diagnosticsService.getMemoryInfo(), message.getBody(String.class) + "\\" + NetworkUtils.getLocalHostName() + "-" + "memory.json");
        } catch (JMSException e) {
            log.error("An exception occurred when trying to get the message body");
        }

        for (String name : this.metricsEndpoint.listNames().getNames()) {
            MetricsEndpoint.MetricResponse actuateMap = metricsEndpoint.metric(name, null);
            Optional<MetricsEndpoint.Sample> optSampleValue = actuateMap.getMeasurements().stream().filter(value -> value.getStatistic().name().equals("VALUE")).findAny();

            if (!SERVER_DATA_LIST.contains(name)) {
                continue;
            }

            serviceMetricsList.add(ServiceMetrics.builder().name(actuateMap.getName())
                    .description(actuateMap.getDescription())
                    .value(optSampleValue.isPresent() ? optSampleValue.get().getValue().toString() : "0")
                    .baseUnit(actuateMap.getBaseUnit())
                    .build());
        }


        try (Writer writer = new FileWriter(message.getBody(String.class) + "\\" + NetworkUtils.getLocalHostName() + "-" + "font.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this.diagnosticsService.getFontsInfo(), writer);
        } catch (Exception e) {
            log.error("An exception occurred when trying to get the message body");
        }


        try {
            Map<String, String> serviceHealth = new HashMap<>();
            serviceHealth.put("name", "service.status");
            serviceHealth.put("value", this.healthEndpoint.health().getStatus().getCode());
            serviceMetricsList.add(serviceHealth);
            writeJsonFile(serviceMetricsList, message.getBody(String.class) + "\\" + NetworkUtils.getLocalHostName() + "-" + serviceName + "-" + System.currentTimeMillis() + ".json");
        } catch (JMSException e) {
            log.error("An exception occurred when trying to get the message body");
        }
    }

    private void writeJsonFile(Object objToBeWritten, String fileName) {
        try (Writer writer = new FileWriter(fileName)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(objToBeWritten, writer);

        } catch (IOException ioe) {
            log.error("Could not create the file", ioe);
        }
    }

}
