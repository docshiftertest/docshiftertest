package com.docshifter.core.config.services;

import com.docshifter.core.metrics.dtos.ServiceHeartbeatDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

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
    private final SimpUserRegistry websocketUserRegistry;
    private final SimpMessagingTemplate websocketTemplate;
    private final Set<Supplier<Set<ServiceHeartbeatDTO>>> heartbeatDTOSuppliers;
    public static final String STOMP_DESTINATION = "/topic/serviceHeartbeats";

    public StatusService(SimpMessagingTemplate websocketTemplate,
                         SimpUserRegistry websocketUserRegistry,
                         ScheduledExecutorService scheduler,
                         @Value("${serviceStatus.heartbeat.delay:5}") int heartbeatDelay,
                         Set<Supplier<Set<ServiceHeartbeatDTO>>> heartbeatDTOSuppliers) {
        this.websocketTemplate = websocketTemplate;
        this.heartbeatDTOSuppliers = heartbeatDTOSuppliers;
        this.websocketUserRegistry = websocketUserRegistry;
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
}
