package com.docshifter.core.config.services;

import com.docshifter.core.metrics.dtos.ServiceHeartbeatDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Extracts and write status service from actuator and DiagnosticsService.
 * This class needs to be a component to be able to configure the JMS listener
 */
@Service
@Log4j2
public class StatusService {
    private final SimpMessagingTemplate websocketTemplate;
    private final ScheduledExecutorService scheduler;
    private final int heartbeatDelay;
    private final Set<Supplier<Set<ServiceHeartbeatDTO>>> heartbeatDTOSuppliers;
    private final Set<ServiceHeartbeatDTO> lastHeartbeats = ConcurrentHashMap.newKeySet();
    private ScheduledFuture<?> runningFuture;
    private final Set<String> sessionsToTrack = new HashSet<>();
    public static final String STOMP_DESTINATION = "/topic/serviceHeartbeats";

    public StatusService(SimpMessagingTemplate websocketTemplate,
                         SimpUserRegistry websocketUserRegistry,
                         ScheduledExecutorService scheduler,
                         @Value("${serviceStatus.heartbeat.delay:5}") int heartbeatDelay,
                         Set<Supplier<Set<ServiceHeartbeatDTO>>> heartbeatDTOSuppliers) {
        this.websocketTemplate = websocketTemplate;
        this.scheduler = scheduler;
        this.heartbeatDelay = heartbeatDelay;
        this.heartbeatDTOSuppliers = heartbeatDTOSuppliers;

        Set<SimpSubscription> subscriptions = websocketUserRegistry.findSubscriptions(this::matchSubscription);
        if (!subscriptions.isEmpty()) {
            subscriptions.stream()
                    .map(SimpSubscription::getSession)
                    .map(SimpSession::getId)
                    .forEach(sessionsToTrack::add);
            configureScheduler(true);
        }
    }

    private void configureScheduler(boolean start) {
        if (start ^ (runningFuture == null || runningFuture.isDone())) {
            log.warn("Received {}, but the future has already {}", start, start ? "started" : "stopped");
            return;
        }

        if (start) {
            runningFuture = scheduler.scheduleWithFixedDelay(this::sendHeartbeats, 0, heartbeatDelay, TimeUnit.SECONDS);
        } else {
            runningFuture.cancel(true);
            runningFuture = null;
            lastHeartbeats.clear();
        }
    }

    private void sendHeartbeats() {
        lastHeartbeats.clear();
        heartbeatDTOSuppliers.stream()
                .map(Supplier::get)
                .flatMap(Set::stream)
                .forEach(this::storeAndSendDto);
    }

    private void storeAndSendDto(ServiceHeartbeatDTO dto) {
        lastHeartbeats.add(dto);
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
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        if (STOMP_DESTINATION.equals(accessor.getDestination())) {
            sessionsToTrack.add(accessor.getSessionId());
            if (runningFuture == null || runningFuture.isDone()) {
                configureScheduler(true);
            } else {
                // Replay last heartbeats (if any) to the client that just subscribed
                for (ServiceHeartbeatDTO heartbeat : lastHeartbeats) {
                    // TODO figure out why this is not reaching the user... Needs proper authentication setup first
                    //  and/or can't send a message to a single user through a topic?
                    Map<String, Object> headers = Map.of(SimpMessageHeaderAccessor.SESSION_ID_HEADER, accessor.getSessionId());
                    try {
                        websocketTemplate.convertAndSendToUser(accessor.getSessionId(), STOMP_DESTINATION, heartbeat, headers);
                    } catch (Exception ex) {
                        log.error("Could not send DTO to user {} at {}", accessor.getSessionId(), STOMP_DESTINATION, ex);
                    }
                }
            }
        }
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        handleLeaveEvent(event);
    }

    /**
     * Can occur in case the client simply closes their browser tab without unsubscribing first (i.e. navigating to a
     * different page).
     * @param event
     */
    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        handleLeaveEvent(event);
    }

    private void handleLeaveEvent(AbstractSubProtocolEvent event) {
        String sessionId = SimpMessageHeaderAccessor.wrap(event.getMessage()).getSessionId();
        if (sessionsToTrack.contains(sessionId)) {
            sessionsToTrack.remove(sessionId);
            if (sessionsToTrack.isEmpty()) {
                configureScheduler(false);
            }
        }
    }
}
