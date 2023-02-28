package com.docshifter.core.config.services;

import com.docshifter.core.config.repositories.ChainConfigurationRepository;
import com.docshifter.core.messaging.dto.DocShifterMessageDTO;
import com.docshifter.core.messaging.message.DocshifterMessage;
import com.docshifter.core.utils.NetworkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.artemis.jms.client.ActiveMQMessage;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.MessageFormatException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.docshifter.core.config.Constants.ONGOING_TASK_QUEUE;

/**
 * @author Juan Marques
 * @created 23/01/2023
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class OngoingTaskService implements IStompService<DocShifterMessageDTO> {

    public static final String STOMP_DESTINATION = "/queue/ongoingTask";
    private static final String USER_QUEUE_ONGOINGTASK_PATTERN = "^(/user/.*/queue/ongoingTask)$";
    private static final String hostName = NetworkUtils.getLocalHostName();
    private final SimpUserRegistry websocketUserRegistry;
    private final SimpMessagingTemplate websocketTemplate;
    private final JmsTemplate ongoingTaskJmsTemplate;
    private final ChainConfigurationRepository chainConfigurationRepository;

    /**
     * Sends the {@link DocShifterMessageDTO} to the subscriptions that match
     * @param dto message to be sent
     */
    @Override
    public void sendDTO(DocShifterMessageDTO dto) {

        try {
            websocketUserRegistry.findSubscriptions(this::matchSubscription)
                    .stream()
                    .map(subscription -> subscription.getSession().getUser().getName())
                    .distinct()
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
     * Sends a {@link DocShifterMessageDTO} to a specific user
     * @param dto dto with the task message
     * @param userName name of the user to send the message
     */
    private void sendDTOToUser(DocShifterMessageDTO dto, String userName) {
        this.websocketTemplate.convertAndSendToUser(userName, STOMP_DESTINATION, dto);
    }

    /**
     * Sends each {@link DocShifterMessageDTO} to the specific user on subscribe
     * @param username name of the user
     * @param destination name of the queue
     * @param ongoingTaskMap Map of taskId and {@link DocShifterMessageDTO} saved
     */
    public void sendOngoingTaskOnSubscribe(String username, String destination, Map<String, DocShifterMessageDTO> ongoingTaskMap) {
        if (destination.matches(USER_QUEUE_ONGOINGTASK_PATTERN) && !ongoingTaskMap.isEmpty()) {
            log.debug("Sending [{}] saved ongoingTask messages to [{}]", ongoingTaskMap.size(), username);
            ongoingTaskMap.forEach((taskId, dto) -> {
                log.debug("Sending message with taskId [{}].", taskId);
                sendDTOToUser(dto, username);
            });
        }
    }

    /**
     * Notifies the console with the ongoing task.
     * @param dsMessage {@link DocshifterMessage} to send to console.
     * @param status status of the task.
     */
    public void notifyConsoleOngoingTask(DocshifterMessage dsMessage, DocShifterMessageDTO.Status status) {
        sendDTOToQueue(convertToDTO(dsMessage, status, null, null, (byte) 0));
    }

    /**
     * Notifies the console with the ongoing task.
     * @param message {@link ActiveMQMessage} to send to console.
     * @param status status of the task.
     */
    public void notifyConsoleOngoingTask(ActiveMQMessage message, DocShifterMessageDTO.Status status) {
        try {
            sendDTOToQueue(convertToDTO(message, status));
        }
        catch (JMSException jmsException) {
            log.error("Failed to convert the message {}", message, jmsException);
        }
    }

    /**
     * Sends the message to the OngoingTask queue
     * @param docShifterMessageDTO  {@link DocShifterMessageDTO} to send
     */
    private void sendDTOToQueue(DocShifterMessageDTO docShifterMessageDTO) {
        try {
            log.debug("Sending ongoing task message: {}", docShifterMessageDTO);
            ongoingTaskJmsTemplate.convertAndSend(ONGOING_TASK_QUEUE, docShifterMessageDTO);
        }
        catch (JmsException jmsException) {
            log.error("Failed to send ongoing task {}", docShifterMessageDTO, jmsException);
        }
    }

    /**
     * Converts {@link ActiveMQMessage} to {@link DocShifterMessageDTO}.
     *
     * @param message the ActiveMQMessage from artemis server.
     * @param status the {@link com.docshifter.core.messaging.dto.DocShifterMessageDTO.Status} for the task.
     * @return DocShifterMessageDTO with message content.
     */
    public DocShifterMessageDTO convertToDTO(ActiveMQMessage message, DocShifterMessageDTO.Status status) throws JMSException {

        DocshifterMessage dsMessage;

        try {
            dsMessage = message.getBody(DocshifterMessage.class);
        }
        catch (MessageFormatException mess) {
            log.error("Message: {} got a format exception!", message, mess);
            Map<String, Object> data = new HashMap<>();
            // Make a kind of placeholder DTO with some default values
            // just so we have something to display in the Q.
            // Note this may cause errors in Console if you try to manage one of these messages,
            // but it's a lesser of two evils approach
            return DocShifterMessageDTO.builder()
                    .messageID(message.getCoreMessage().getMessageID())
                    .messagePriority((byte) 0)
                    .configId(999L)
                    .type("DEFAULT")
                    .workflowName("Unknown Workflow")
                    .taskID("No Task Id")
                    .filename("Filename unknown")
                    .sourceFilePath("No Path")
                    .data(data)
                    .hostname(hostName)
                    .status(status.getValue())
                    .build();
        }

        return convertToDTO(
                dsMessage,
                status,
                message.getJMSMessageID(),
                message.getCoreMessage().getMessageID(),
                message.getCoreMessage().getPriority()
        );
    }

    /**
     * Converts {@link DocshifterMessage} to {@link DocShifterMessageDTO}.
     *
     * @param dsMessage the {@link DocshifterMessage} to convert.
     * @param status the {@link com.docshifter.core.messaging.dto.DocShifterMessageDTO.Status} for the task.
     * @return DocShifterMessageDTO with message content.
     */
    private DocShifterMessageDTO convertToDTO(DocshifterMessage dsMessage,
                                              DocShifterMessageDTO.Status status,
                                              String jmsMessageID,
                                              Long messageID,
                                              byte priority) {

        String filename = Paths.get(dsMessage.getTask().getSourceFilePath()).getFileName().toString();

        String workflowName = chainConfigurationRepository.findWorkflowNameById(dsMessage.getConfigId());

        return DocShifterMessageDTO.builder()
                .jmsMessageID(jmsMessageID)
                .messageID(messageID)
                .messagePriority(priority)
                .configId(dsMessage.getConfigId())
                .type(dsMessage.getType().name())
                .workflowName(workflowName)
                .taskID(dsMessage.getTask().getId())
                .filename(filename)
                .sourceFilePath(dsMessage.getTask().getSourceFilePath())
                // Adding this because we just need to send the taskData once at the first status
                .data(DocShifterMessageDTO.Status.isWaitingToBeProcessed(status.getValue()) ? dsMessage.getTask().getData() : null)
                .hostname(hostName)
                .status(status.getValue())
                .build();
    }

}
