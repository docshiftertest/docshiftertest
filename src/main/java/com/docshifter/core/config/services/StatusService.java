package com.docshifter.core.config.services;

import com.docshifter.core.metrics.dtos.ServiceHeartbeatDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Extracts and write status service from actuator and DiagnosticsService.
 * This class needs to be a component to be able to configure the JMS listener
 */
@Service
@Log4j2
public class StatusService {
    private Instant appReadyTime;
    private final ScheduledExecutorService scheduler;
    private final SimpUserRegistry websocketUserRegistry;
    private final SimpMessagingTemplate websocketTemplate;
    private final Set<Supplier<Set<ServiceHeartbeatDTO>>> heartbeatDTOSuppliers;
    public static final String STOMP_DESTINATION = "/topic/serviceHeartbeats";
    public static final String RESTART_SERVICE_JMS_QUEUE = "/queue/restartService";

    public StatusService(SimpMessagingTemplate websocketTemplate,
                         SimpUserRegistry websocketUserRegistry,
                         ScheduledExecutorService scheduler,
                         @Value("${serviceStatus.heartbeat.delay:5}") int heartbeatDelay,
                         Set<Supplier<Set<ServiceHeartbeatDTO>>> heartbeatDTOSuppliers) {
        this.websocketTemplate = websocketTemplate;
        this.heartbeatDTOSuppliers = heartbeatDTOSuppliers;
        this.websocketUserRegistry = websocketUserRegistry;
        this.scheduler = scheduler;
        scheduler.scheduleWithFixedDelay(this::sendHeartbeats, 0, heartbeatDelay, TimeUnit.SECONDS);
    }

    private void sendHeartbeats() {
        if (websocketUserRegistry.findSubscriptions(this::matchSubscription).isEmpty()) {
            return;
        }
        heartbeatDTOSuppliers.stream()
                .map(Supplier::get)
                .flatMap(Set::stream)
                .forEach(this::sendDto);
    }

    private void sendDto(ServiceHeartbeatDTO dto) {
        try {
            websocketTemplate.convertAndSend(STOMP_DESTINATION, dto);
        } catch (Exception ex) {
            log.error("Could not send DTO to {}", STOMP_DESTINATION, ex);
        }
    }

    private boolean matchSubscription(SimpSubscription subscription) {
        return STOMP_DESTINATION.equals(subscription.getDestination());
    }

    @EventListener
    public void onAppReady(HealthManagementService.FirstCorrectFiredEvent event) {
        appReadyTime = Instant.now();
    }

    @JmsListener(destination = RESTART_SERVICE_JMS_QUEUE + "/#{T(com.docshifter.core.metrics.dtos.ServiceHeartbeatDTO)" +
            ".generateComponentId('${spring.application.name}')}")
    public boolean restartService() {
        if (appReadyTime == null || appReadyTime.plusSeconds(30).isAfter(Instant.now())) {
            log.info("Received a restart command, but application has recently started up, so will ignore it!");
            return false;
        }
        log.info("Exiting the current application in 1 second after having received a restart command.");
        scheduler.schedule(() -> System.exit(1), 1, TimeUnit.SECONDS);
        return true;
    }
}
