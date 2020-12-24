package com.docshifter.core;

import com.docshifter.core.config.Constants;
import com.docshifter.core.config.service.ConfigurationService;
import com.docshifter.core.config.service.GeneralConfigService;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 6/9/16.
 */
@Configuration
@ComponentScan(basePackages = {"com.docshifter.core", "com.docshifter.core.monitoring"})
@EnableJpaRepositories(basePackages = {
        "com.docshifter.core.config.domain",
        "com.docshifter.core.monitoring.repo"})
@EntityScan({"com.docshifter.core.config", "com.docshifter.core.monitoring.entities"})
public class DocShifterConfiguration {

	@Value("${queue.replytimeout:300}")
	private int queueReplyTimeout;

	public GeneralConfigService generalConfigService;
	
	public ConfigurationService configurationService;
	
	@Autowired
	public DocShifterConfiguration(GeneralConfigService generalConfigService,
			ConfigurationService configurationService) {
		this.generalConfigService = generalConfigService;
		this.configurationService = configurationService;
	}
	
	public DocShifterConfiguration () {
		
	}

	/**
	 * Creates the initial connection with the information stored in the database.
	 * @return activemq connection factory
	 */
	public ActiveMQConnectionFactory activeMQConnectionFactory() {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(generalConfigService.getString(Constants.MQ_URL));
		connectionFactory.setUser(generalConfigService.getString(Constants.MQ_USER));
		connectionFactory.setPassword(generalConfigService.getString(Constants.MQ_PASSWORD));		
		//Forces consumers to ask for new messages instead buffering the messages.
        connectionFactory.setConsumerWindowSize(0);
		return connectionFactory;
	}

	@Bean
	public org.springframework.jms.connection.CachingConnectionFactory cachingConnectionFactory() {
		return new org.springframework.jms.connection.CachingConnectionFactory(activeMQConnectionFactory());
	}

	@Bean
	public JmsTemplate jmsTemplate() {
		JmsTemplate template = new JmsTemplate(cachingConnectionFactory());
		template.setReceiveTimeout(queueReplyTimeout);
	    template.setExplicitQosEnabled(true);
	    template.setDeliveryPersistent(true);
		return template;
	}

	@Bean
	@DependsOn("jmsTemplate")
	public JmsMessagingTemplate messagingTemplate () {
		return new JmsMessagingTemplate(jmsTemplate());
	}

	@Bean
	public JmsTemplate jmsTemplateMulticast() {
		JmsTemplate template = new JmsTemplate(cachingConnectionFactory());
		template.setPubSubDomain(true);
		template.setPriority(9);
		return template;
	}

	@Bean
	public List<ActiveMQQueue> docShifterQueues() {
		List<ActiveMQQueue> queueList = new ArrayList<>();
		queueList.add(defaultQueue());

		return queueList;
	}

	@Bean
	public ActiveMQQueue defaultQueue() {
		return new ActiveMQQueue(generalConfigService.getString(Constants.MQ_QUEUE),false);
	}

	@Bean
	public ActiveMQTopic reloadExchange() {
		return new ActiveMQTopic(Constants.RELOAD_QUEUE,false);
	}

	@Bean
	public ActiveMQQueue syncQueue() {
		return new ActiveMQQueue(Constants.SYNC_QUEUE,false);
	}
}
