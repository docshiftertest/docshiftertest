package com.docshifter.core.config;

import com.docshifter.core.config.services.HealthManagementService;
import com.docshifter.core.metrics.dtos.ServiceHeartbeatDTO;
import com.docshifter.core.utils.NetworkUtils;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.context.event.EventListener;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import javax.jms.JMSException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Extracts and write status service from actuator and DiagnosticsService.
 * This class needs to be a component to be able to configure the JMS listener
 */
@Service
@Log4j2
public class StatusService {

    private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final String applicationName;
    private final ApplicationAvailability applicationAvailability;
    private final DataSourceHealthIndicator dataSourceHealthIndicator;
    protected final SimpMessagingTemplate websocketTemplate;
    protected final SimpUserRegistry websocketUserRegistry;
    private final ScheduledExecutorService scheduler;
    private final InstallationType installationType;
    private final HealthManagementService healthManagementService;
    private final FileStore currFileStore;
    private ScheduledFuture<?> runningFuture;
    public static final String STOMP_DESTINATION = "/topic/serviceHeartbeats";

    public StatusService(@Value("${spring.application.name:unnamed}") String applicationName,
                         ApplicationAvailability applicationAvailability,
                         DataSourceHealthIndicator dataSourceHealthIndicator,
                         SimpMessagingTemplate websocketTemplate,
                         SimpUserRegistry websocketUserRegistry,
                         ScheduledExecutorService scheduler,
                         InstallationType installationType,
                         HealthManagementService healthManagementService) throws IOException {
        this.applicationName = applicationName;
        this.applicationAvailability = applicationAvailability;
        this.dataSourceHealthIndicator = dataSourceHealthIndicator;
        this.websocketTemplate = websocketTemplate;
        this.websocketUserRegistry = websocketUserRegistry;
        this.scheduler = scheduler;
        this.installationType = installationType;
        this.healthManagementService = healthManagementService;
        this.currFileStore = Files.getFileStore(Path.of(""));

        if (!websocketUserRegistry.findSubscriptions(this::matchSubscription).isEmpty()) {
            configureScheduler(true);
        }
    }

    /**
     * Writes the service status into files located in the workFolder directory (the folder is located into the queue message)
     * If got an exception writing a file or getting the body message, It would show the log and continue processing the rest.
     * The containerFactory is needed otherwise it won't know the topic to listen to and will never pick up any request.
     *
     * @param message : The workfolder path.
     */
    @JmsListener(destination = Constants.STATUS_QUEUE, containerFactory = Constants.TOPIC_LISTENER)
    public void serviceStatus(ActiveMQMessage message) {
        boolean start;
        try {
            start = message.getBody(Boolean.class);
        } catch (JMSException ex) {
            log.error("Unable to deserialize message body.", ex);
            return;
        }

        configureScheduler(start);
    }

    private void configureScheduler(boolean start) {
        if (start ^ runningFuture == null) {
            log.error("Received {} in message body, but the future has already {}", start, start ? "started" :"stopped");
            return;
        }

        if (start) {
            runningFuture = scheduler.scheduleWithFixedDelay(this::sendHeartbeat, 0, 5, TimeUnit.SECONDS);
        } else {
            runningFuture.cancel(true);
            runningFuture = null;
        }
    }

    protected void sendHeartbeat() {
        Long diskTotal = null, diskUsed = null;
        try {
            diskTotal = currFileStore.getTotalSpace();
            diskUsed = diskTotal - currFileStore.getUsableSpace();
        } catch (IOException ex) {
            log.error("Could not fetch disk info for current file store", ex);
        }

        ServiceHeartbeatDTO.DataPoints instance = new ServiceHeartbeatDTO.DataPoints(
                NetworkUtils.getLocalHostName(),
                osBean.getCpuLoad(),
                osBean.getFreeMemorySize(),
                osBean.getTotalMemorySize(),
                diskUsed,
                diskTotal,
                null,
                null
        );

        ServiceHeartbeatDTO.DataPoints jvmComponent = new ServiceHeartbeatDTO.DataPoints(
                applicationName,
                osBean.getProcessCpuLoad(),
                Runtime.getRuntime().freeMemory(),
                Runtime.getRuntime().maxMemory(),
                diskUsed,
                diskTotal,
                null,
                null
        );

        ServiceHeartbeatDTO dto = new ServiceHeartbeatDTO(
                ZonedDateTime.now(),
                ServiceHeartbeatDTO.InstallationType.mapFrom(installationType),
                ServiceHeartbeatDTO.Status.mapFrom(healthManagementService.isAppReady(),
                        applicationAvailability.getLivenessState(),
                        dataSourceHealthIndicator.health()),
                instance,
                jvmComponent);
        websocketTemplate.convertAndSend(STOMP_DESTINATION, dto);
    }

    protected boolean matchSubscription(SimpSubscription subscription) {
        return STOMP_DESTINATION.equals(subscription.getDestination());
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        if (runningFuture == null
                && STOMP_DESTINATION.equals(SimpMessageHeaderAccessor.wrap(event.getMessage()).getDestination())) {
            configureScheduler(true);
        }
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        if (runningFuture != null
                // TODO destination always null? Keep track of session ID and link to dest?
                && STOMP_DESTINATION.equals(SimpMessageHeaderAccessor.wrap(event.getMessage()).getDestination())
                && websocketUserRegistry.findSubscriptions(this::matchSubscription).isEmpty()) {
            configureScheduler(false);
        }
    }


}
