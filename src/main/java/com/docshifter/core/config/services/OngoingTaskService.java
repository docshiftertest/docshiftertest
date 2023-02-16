package com.docshifter.core.config.services;

import com.docshifter.core.metrics.dtos.OngoingTaskDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.docshifter.core.config.Constants.ONGOING_TASK_QUEUE;

/**
 * @author Juan Marques
 * @created 23/01/2023
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class OngoingTaskService implements IStompService<OngoingTaskDTO> {

    public static final String STOMP_DESTINATION = "/queue/ongoingTask";
    private static final String USER_QUEUE_ONGOINGTASK_PATTERN = "^(/user/.*/queue/ongoingTask)$";
    private final SimpUserRegistry websocketUserRegistry;
    private final SimpMessagingTemplate websocketTemplate;
    private final JmsTemplate ongoingTaskJmsTemplate;
    private final ObjectMapper mapper;

    /**
     * Sends the {@link OngoingTaskDTO} to the subscriptions that match
     * @param dto message to be sent
     */
    @Override
    public void sendDTO(OngoingTaskDTO dto) {

        try {
            websocketUserRegistry.findSubscriptions(this::matchSubscription)
                    .stream()
                    .map(subscription -> subscription.getSession().getUser().getName())
                    .forEach(userName -> {
                        sendDTOToUser(dto, userName);
                        log.debug("Sent the message to the queue: [{}] for [{}}].",
                                STOMP_DESTINATION, userName);
                    });
        }
        catch (Exception ex) {
            log.error("Could not send DTO to {}", STOMP_DESTINATION, ex);
        }
    }

    /**
     * Checks if the destination for a subscription matches the pattern
     * @param subscription the subscription to be checked
     * @return either if the subscription matches or not
     */
    @Override
    public boolean matchSubscription(SimpSubscription subscription) {
        return subscription.getDestination().matches(USER_QUEUE_ONGOINGTASK_PATTERN);
    }

    /**
     * Sends a {@link OngoingTaskDTO} to a specific user
     * @param dto dto with the task message
     * @param userName name of the user to send the message
     */
    private void sendDTOToUser(OngoingTaskDTO dto, String userName) {
        this.websocketTemplate.convertAndSendToUser(userName, STOMP_DESTINATION, dto);
    }

    /**
     * Sends each {@link OngoingTaskDTO} to the specific user on subscribe
     * @param username name of the user
     * @param destination name of the queue
     * @param ongoingTaskSet set of {@link OngoingTaskDTO} saved
     */
    public void sendOngoingTaskOnSubscribe(String username, String destination, Set<OngoingTaskDTO> ongoingTaskSet) {
        if (destination.matches(USER_QUEUE_ONGOINGTASK_PATTERN) && !ongoingTaskSet.isEmpty()) {
            log.debug("Sending [{}] saved ongoingTask messages to [{}]", ongoingTaskSet.size(), username);
            ongoingTaskSet.forEach(dto -> sendDTOToUser(dto, username));
        }
    }

    /**
     * Notify the console with the ongoing task
     * @param ongoingTaskDTO dto with the data to notify
     */
    public void notifyConsoleOngoingTask(OngoingTaskDTO ongoingTaskDTO) {
        try {
            log.debug("Sending ongoing task message: {}", ongoingTaskDTO);
            ongoingTaskJmsTemplate.convertAndSend(ONGOING_TASK_QUEUE, toJson(ongoingTaskDTO));
        }
        catch (JmsException jmsException) {
            log.error("Failed to send ongoing task {}", ongoingTaskDTO, jmsException);
        }
        catch (JsonProcessingException jsonProcessingException) {
            log.error("Failed to convert message {}", ongoingTaskDTO, jsonProcessingException);
        }
    }

    /**
     * Converts the {@link OngoingTaskDTO} to json format
     * @param ongoingTaskDTO message to be converted
     * @return the String in json format
     * @throws JsonProcessingException while trying to convert the message
     */
    private String toJson(OngoingTaskDTO ongoingTaskDTO) throws JsonProcessingException {
        return mapper.writeValueAsString(ongoingTaskDTO);
    }

}
