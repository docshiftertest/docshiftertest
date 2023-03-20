package com.docshifter.core.config.services;

import org.springframework.messaging.simp.user.SimpSubscription;

/**
 * @author Juan Marques
 * @created 24/01/2023
 */
public interface IStompService<T> {

    /**
     * Sends the dto to a queue or topic
     * @param dto message to be sent
     */
    void sendDTO(T dto);

    /**
     * checks if the {@link SimpSubscription} matches the destination
     * @param subscription the subscription to be checked
     * @return if matches the destination or not
     */
    boolean matchSubscription(SimpSubscription subscription);
}
