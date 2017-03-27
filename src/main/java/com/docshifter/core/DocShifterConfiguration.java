package com.docshifter.core;

import com.docshifter.core.config.Constants;
import com.docshifter.core.config.domain.GlobalSettingsRepository;
import com.docshifter.core.config.service.ConfigurationService;
import com.docshifter.core.config.service.GeneralConfigService;
import com.docshifter.core.work.WorkFolderManager;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michiel.vandriessche@docbyte.com on 6/9/16.
 */
@Configuration
@ComponentScan
@EnableJpaRepositories(basePackages = {"com.docshifter.core.config.domain"})
public class DocShifterConfiguration {

	@Autowired
	public GeneralConfigService generalConfigService;

	@Autowired
	public ConfigurationService configurationService;

	@Autowired
	public WorkFolderManager workFolderManager;


	@Bean
	public ConnectionFactory connectionFactory() {

		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(generalConfigService.getString(Constants.MQ_URL));
		connectionFactory.setUsername(generalConfigService.getString(Constants.MQ_USER));
		connectionFactory.setPassword(generalConfigService.getString(Constants.MQ_PASSWORD));
		return connectionFactory;
	}

	@Bean
	public MessageConverter jsonMessageConverter(){
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate() {
		RabbitTemplate template = new RabbitTemplate(connectionFactory());
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}

	@Bean
	public List<Queue> docShifterQueues() {
		List<Queue> queueList = new ArrayList<>();
		queueList.add(defaultQueue());

		return  queueList;
	}

	@Bean
	public Queue defaultQueue() {
		Map<String, Object> args = new HashMap<>();
		args.put("x-max-priority", 4);

		return new Queue(generalConfigService.getString(Constants.MQ_QUEUE), true, false, false, args);

	}

	@Bean
	public FanoutExchange reloadExchange() {
		return new FanoutExchange(Constants.RELOAD_QUEUE);
	}

	@Bean
	public Queue syncQueue() {
		return new Queue(Constants.SYNC_QUEUE);

	}
}
