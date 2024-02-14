package com.docshifter.core.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Log4j2
public class CacheService {

    /**
     * Clears all entries from the specified cache upon receiving a message on the designated JMS topic.
     * This is intended to ensure the cache is refreshed with the latest configurations.
     *
     * @param cacheManager The cache manager used to access the cache.
     */
    @JmsListener(destination = Constants.RELOAD_QUEUE, containerFactory = Constants.TOPIC_LISTENER)
    @CacheEvict(value = Constants.SENDER_CONFIGURATION_CACHE, allEntries = true)
    public void refreshCache(@Autowired CacheManager cacheManager) {
        log.info("Initiating cache refresh...");
        cacheManager.getCacheNames().stream()
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .forEach(Cache::clear);
    }
}
