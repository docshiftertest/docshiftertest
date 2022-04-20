package com.docshifter.core.config;

import com.docshifter.core.config.services.DiagnosticsService;
import com.docshifter.core.metrics.dtos.ServiceHealthDTO;
import com.docshifter.core.metrics.dtos.ServiceMetrics;
import com.docshifter.core.utils.NetworkUtils;
import com.docshifter.core.work.WorkFolderManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import net.sf.ehcache.CacheManager;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpoint;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.management.HeapDumpWebEndpoint;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;


import lombok.extern.log4j.Log4j2;
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
import java.util.TreeMap;

/**
 * @author Juan Marques created on 28/01/2021
 */

/**
 * Central cache configuration responsible for cleaning sender configuration and 2nd level cache from hibernate - ehcache.
 */
@Configuration
@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
@EnableCaching
@Log4j2
@Component
public class CacheConfiguration {

    private final HealthEndpoint healthEndpoint;
    private final MetricsEndpoint metricsEndpoint;
    private final EnvironmentEndpoint environmentEndpoint;
    private final InfoEndpoint infoEndpoint;
    private final BeansEndpoint beansEndpoint;
    private final WorkFolderManager workFolderManager;

    //	private final AuditEventsEndpoint auditEventsEndpoint;
    private final ConditionsReportEndpoint conditionsReportEndpoint;
    private final ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint;
    //	private final FlywayEndpoint flywayEndpoint;
    private final HeapDumpWebEndpoint heapDumpWebEndpoint;
//	private final LiquibaseEndpoint liquibaseEndpoint;
//	private final LogFileWebEndpoint logFileWebEndpoint;

    private final LoggersEndpoint loggersEndpoint;
    //	private final PrometheusScrapeEndpoint prometheusScrapeEndpoint;
//	private final SessionsEndpoint sessionsEndpoint;
    private final ThreadDumpEndpoint threadDumpEndpoint;

    private final DiagnosticsService diagnosticsService;

    private static final List<String> SERVER_DATA_LIST =
            Arrays.asList("disk.free", "disk.total", "jvm.memory.max", "jvm.memory.used",
                    "process.cpu.usage", "system.cpu.usage", "jvm.memory.committed");

    @Autowired(required = false)
    private KubernetesClient k8sClient;

    public CacheConfiguration(DiagnosticsService diagnosticsService, HealthEndpoint healthEndpoint, MetricsEndpoint metricsEndpoint, EnvironmentEndpoint environmentEndpoint, InfoEndpoint infoEndpoint, BeansEndpoint beansEndpoint, WorkFolderManager workFolderManager, ConditionsReportEndpoint conditionsReportEndpoint, ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint, HeapDumpWebEndpoint heapDumpWebEndpoint, LoggersEndpoint loggersEndpoint, ThreadDumpEndpoint threadDumpEndpoint) {
        this.healthEndpoint = healthEndpoint;
        this.metricsEndpoint = metricsEndpoint;
        this.environmentEndpoint = environmentEndpoint;
        this.infoEndpoint = infoEndpoint;
        this.beansEndpoint = beansEndpoint;
        this.workFolderManager = workFolderManager;
        this.conditionsReportEndpoint = conditionsReportEndpoint;
        this.configurationPropertiesReportEndpoint = configurationPropertiesReportEndpoint;
        this.heapDumpWebEndpoint = heapDumpWebEndpoint;
        this.loggersEndpoint = loggersEndpoint;
        this.threadDumpEndpoint = threadDumpEndpoint;
        this.diagnosticsService = diagnosticsService;
    }

    /**
     * We use a different container factory because is necessary to enable pub/sub mode in order to receive messages from topic
     * In receiver case it is working with two JMS listener containers one to listen to the default queue and another to topics.
     */
    @JmsListener(destination = Constants.RELOAD_QUEUE, containerFactory = Constants.TOPIC_LISTENER)
    @CacheEvict(value = Constants.SENDER_CONFIGURATION_CACHE, allEntries = true)
    public void cacheCleaner() {
        log.info("Cleaning cache config....");
        CacheManager.ALL_CACHE_MANAGERS.forEach(CacheManager::clearAll);
    }

    @JmsListener(destination = Constants.STATUS_QUEUE, containerFactory = Constants.TOPIC_LISTENER)
    public void serviceStatus(ActiveMQMessage message) {
//        List<ServiceMetrics> serviceMetricsList = new ArrayList<>();
        List<Object> serviceMetricsList = new ArrayList<>();

        log.info("Doing something here");

        SystemHealth healthComponent = (SystemHealth) this.healthEndpoint.health();
//		Map<String, HealthComponent> healthComponentsMap = healthComponent.getComponents();
//		((SystemHealth) healthComponent).components.get("db");
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
//        try (Writer writer = new FileWriter(message.getBody(String.class) + "\\" + "db.json")) {
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            gson.toJson(healthDTO, writer);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //memory info

        try {
            writeJsonFile(this.diagnosticsService.getMemoryInfo(), message.getBody(String.class) + "\\" + NetworkUtils.getLocalHostName()+ "-" + "memory.json");
        } catch (JMSException e) {
            log.error("An exception occurred when trying to get the message body");
        }
//        try (Writer writer = new FileWriter(message.getBody(String.class) + "\\" + "memory.json")) {
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            gson.toJson(this.diagnosticsService.getMemoryInfo(), writer);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


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


        try (Writer writer = new FileWriter(message.getBody(String.class) + "\\" + NetworkUtils.getLocalHostName()+ "-" + "font.json")) {
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
            writeJsonFile(serviceMetricsList, message.getBody(String.class) + "\\" + NetworkUtils.getLocalHostName()+ "-" + serviceName + "-" + System.currentTimeMillis() + ".json");
        } catch (JMSException e) {
            log.error("An exception occurred when trying to get the message body");
        }
//        try (Writer writer = new FileWriter(message.getBody(String.class) + "\\" + serviceName + "-" + System.currentTimeMillis() + ".json")) {
////			file = new File(workFolder.getFolder().toString(), serviceName + ".json");
////			log.debug("Path to temporary zip file: " + file.getAbsolutePath());
////			file.createNewFile();
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            gson.toJson(serviceMetricsList, writer);
////			new GsonBuilder().setPrettyPrinting().create().toJson(serviceMetricsList, new FileWriter(file.getAbsolutePath().toString()));
//
//        } catch (IOException ioe) {
//            log.error("Could not read imported module", ioe);
////			success.setSuccess(false);
////			throw new IllegalArgumentException("Could not read imported module!", ioe);
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }

//		k8sClient.pods();

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
