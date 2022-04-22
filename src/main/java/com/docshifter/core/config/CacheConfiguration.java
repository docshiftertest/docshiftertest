package com.docshifter.core.config;

import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.CacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;

/**
 * @author Juan Marques created on 28/01/2021
 *
 */

/**
 * Central cache configuration responsible for cleaning sender configuration and 2nd level cache from hibernate - ehcache.
 */
@Configuration
@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
@EnableCaching
@Log4j2
public class CacheConfiguration {

	/**
	 * We use a different container factory because is necessary to enable pub/sub mode in order to receive messages from topic
	 * In receiver case it is working with two JMS listener containers one to listen to the default queue and another to topics.
	 */
	@JmsListener(destination = Constants.RELOAD_QUEUE, containerFactory = Constants.TOPIC_LISTENER)
	@CacheEvict(value = Constants.SENDER_CONFIGURATION_CACHE, allEntries = true)
	public void cacheCleaner() {
		log.info("Cleaning cache config....");
		CacheManager.ALL_CACHE_MANAGERS.forEach(CacheManager::clearAll);
	}

}
