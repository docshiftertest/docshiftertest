/**
 * 
 */
package com.docshifter.core.security;

import javax.persistence.EntityManagerFactory;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Processor to register the encryption listener.
 * 
 * @author Created by juan.marques on 09/12/2019.
 */
@Component
public class EncryptionBeanPostProcessor implements BeanPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(EncryptionBeanPostProcessor.class);

	private EncryptionListener encryptionListener;

	public EncryptionBeanPostProcessor(EncryptionListener encryptionListener) {
		this.encryptionListener = encryptionListener;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

		if (bean instanceof EntityManagerFactory) {

			EntityManagerFactory entityManagerFactory = (EntityManagerFactory) bean;

			SessionFactoryImplementor sessionFactoryImpl = entityManagerFactory.unwrap(SessionFactoryImplementor.class);

			EventListenerRegistry registry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);

			registry.appendListeners(EventType.POST_LOAD, encryptionListener);
			registry.appendListeners(EventType.PRE_INSERT, encryptionListener);
			registry.appendListeners(EventType.PRE_UPDATE, encryptionListener);
			registry.appendListeners(EventType.PRE_COLLECTION_UPDATE, encryptionListener);

			logger.debug("Encryption listener has been successfully set up");
		}
		return bean;
	}
}
