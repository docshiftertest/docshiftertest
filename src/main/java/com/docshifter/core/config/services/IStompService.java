package com.docshifter.core.config.services;

import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;

/**
 * @author Juan Marques
 * @created 24/01/2023
 */
public interface IStompService<T> {

    void sendDTO(T dto);

    default boolean areSubscriptionsEmpty(SimpUserRegistry websocketUserRegistry) {
        return websocketUserRegistry.findSubscriptions(this::matchSubscription).isEmpty();
    }

    boolean matchSubscription(SimpSubscription subscription);
}
