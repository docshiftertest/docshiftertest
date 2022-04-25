package com.docshifter.core.config;

import com.docshifter.core.config.services.DiagnosticsService;
import com.docshifter.core.metrics.dtos.ServiceHealthDTO;
import com.docshifter.core.metrics.dtos.ServiceMetrics;
import com.docshifter.core.utils.FileUtils;
import com.docshifter.core.utils.NetworkUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.JMSException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
@Log4j2
public class ServicesStatusConfiguration {

    private final HealthEndpoint healthEndpoint;
    private final MetricsEndpoint metricsEndpoint;
    private final InfoEndpoint infoEndpoint;

    private final DiagnosticsService diagnosticsService;

    public static final List<String> SERVER_DATA_LIST = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("disk.free");
                add("disk.total");
                add("jvm.memory.max");
                add("jvm.memory.used");
                add("jvm.memory.committed");
                add("system.cpu.usage");
            }});

    public ServicesStatusConfiguration(DiagnosticsService diagnosticsService, HealthEndpoint healthEndpoint, MetricsEndpoint metricsEndpoint, InfoEndpoint infoEndpoint) {
        this.healthEndpoint = healthEndpoint;
        this.metricsEndpoint = metricsEndpoint;
        this.infoEndpoint = infoEndpoint;
        this.diagnosticsService = diagnosticsService;
    }

    /**
     * Writes the service status into files located in the workFolder directory (the folder is located into the queue message)
     */
    @JmsListener(destination = Constants.STATUS_QUEUE, containerFactory = Constants.TOPIC_LISTENER)
    public void serviceStatus(ActiveMQMessage message) {
        List<Object> serviceMetricsList = new ArrayList<>();

        SystemHealth healthComponent = (SystemHealth) this.healthEndpoint.health();
        Map<String, String> dbMap = new HashMap<>();

        Map<String, Object> infoEndpointMap = this.infoEndpoint.info();

        if (infoEndpointMap == null) {
            return;
        }

        Map<String, Object> metricsMap = (Map<String, Object>) infoEndpointMap.get("build");
        Object nameObj = metricsMap.get("name");

        if (nameObj == null) {
            log.error("Could not get the service name");
            return;
        }

        String serviceName = nameObj.toString();
        /*
         * The HealthComponent provides detailed information about the health of the application.
         *
         * HealthComponent possible values:
         * "db" ->  "metricsDataSource" -> {Health@18048} "UP {database=PostgreSQL, validationQuery=isValid()}"
                     "docshifterDataSource" -> {Health@18046} "UP {database=PostgreSQL, validationQuery=isValid()}"
         * "diskSpace" -> {Health@15674} "UP {total=107373101056, free=54983962624, threshold=10485760, exists=true}"
         * "jms" -> {Health@15676} "UP {provider=ActiveMQ}"
         * "ping" -> {Health@15678} "UP {}"
         */
        for (Map.Entry<String, HealthComponent> healthComponentsMap : healthComponent.getComponents().entrySet()) {
            if (healthComponentsMap.getKey().equals("db")) {
                ((CompositeHealth) ((Map.Entry) healthComponentsMap).getValue()).getComponents().forEach(
                        (key, value1) -> dbMap.put(key, value1.getStatus().getCode()));
            }
        }

        //health info
        ServiceHealthDTO healthDTO = ServiceHealthDTO.builder().
                status(healthComponent.getStatus().toString()).
                dbStatus(dbMap).
                build();

        try {
            FileUtils.writeJsonFile(healthDTO, message.getBody(String.class) + File.separator + NetworkUtils.getLocalHostName() + "-db-" + System.currentTimeMillis() + ".json");
        } catch (JMSException e) {
            //Only shows the log because more data can be shown
            log.error("An exception occurred when trying to get the message db body", e);
        }

        try {
            FileUtils.writeJsonFile(this.diagnosticsService.getMemoryInfo(), message.getBody(String.class) + File.separator + NetworkUtils.getLocalHostName() + "-" + "memory.json");
        } catch (JMSException e) {
            //Only shows the log because more data can be shown
            log.error("An exception occurred when trying to get the memory message body", e);
        }

        /* Can have more than a hundred data
         * 0 = "application.ready.time"
         *   1 = "application.started.time"
         *   2 = "disk.free"
         *   3 = "disk.total"
         *   4 = "hikaricp.connections"
         */

        // goes through all metric endpoint names to write only files that are inside SERVER_DATA_LIST
        for (String name : this.metricsEndpoint.listNames().getNames()) {
            MetricsEndpoint.MetricResponse actuateMap = metricsEndpoint.metric(name, null);
            Optional<MetricsEndpoint.Sample> optSampleValue = actuateMap.getMeasurements().stream().filter(value -> value.getStatistic().name().equals("VALUE")).findAny();

            // if it's not on the list, continue
            if (!SERVER_DATA_LIST.contains(name)) {
                continue;
            }

            serviceMetricsList.add(ServiceMetrics.builder().name(actuateMap.getName())
                    .description(actuateMap.getDescription())
                    .value(optSampleValue.isPresent() ? optSampleValue.get().getValue().toString() : "0")
                    .baseUnit(actuateMap.getBaseUnit())
                    .build());
        }

        try {
            // Adds the service status into the map of results.
            Map<String, String> serviceHealth = new HashMap<>();
            serviceHealth.put("name", "service.status");
            serviceHealth.put("value", this.healthEndpoint.health().getStatus().getCode());
            serviceMetricsList.add(serviceHealth);

            FileUtils.writeJsonFile(serviceMetricsList, message.getBody(String.class) + File.separator + NetworkUtils.getLocalHostName() + "-" + serviceName + "-" + System.currentTimeMillis() + ".json");
        } catch (JMSException e) {
            //Only shows the log because more data can be shown
            log.error("An exception occurred when trying to get the services message body!", e);
        }
    }



}
