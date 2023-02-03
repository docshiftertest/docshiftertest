package com.docshifter.core.config.services.impl;

import com.docshifter.core.config.services.IJmsTemplateFactory;
import com.docshifter.core.config.services.IStompService;
import com.docshifter.core.metrics.dtos.OngoingTaskDTO;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import static com.docshifter.core.config.Constants.ONGOING_TASK_QUEUE_DESTINATION;
import static com.docshifter.core.config.Constants.STOMP_ONGOING_TASK_DESTINATION;

/**
 * @author Juan Marques
 * @created 23/01/2023
 */
@Log4j2
@AllArgsConstructor
@Service
public class OngoingTaskService implements IStompService<OngoingTaskDTO> {

    private final SimpUserRegistry websocketUserRegistry;
    private final SimpMessagingTemplate websocketTemplate;
    private final IJmsTemplateFactory jmsTemplateFactory;

    @Override
    public void sendDTO(OngoingTaskDTO dto) {
        if (areSubscriptionsEmpty(websocketUserRegistry)) {
            return;
        }
        try {
            this.websocketTemplate.convertAndSend(STOMP_ONGOING_TASK_DESTINATION, dto);
        } catch (Exception ex) {
            log.error("Could not send DTO to {}", STOMP_ONGOING_TASK_DESTINATION, ex);
        }
    }

    @Override
    public boolean matchSubscription(SimpSubscription subscription) {
        return STOMP_ONGOING_TASK_DESTINATION.equals(subscription.getDestination());
    }

    public void notifyConsoleOngoingTask(OngoingTaskDTO ongoingTaskDTO) {
        try {
            JmsTemplate jmsTemplate = jmsTemplateFactory.create(9, 0, 0);
            jmsTemplate.convertAndSend(ONGOING_TASK_QUEUE_DESTINATION, ongoingTaskDTO);
        } catch (JmsException jmsException) {
            log.error("Failed to send ongoing task {}",ongoingTaskDTO);
        }
    }
}
