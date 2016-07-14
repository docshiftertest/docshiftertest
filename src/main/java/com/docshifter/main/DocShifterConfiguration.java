package com.docshifter.main;

import com.docbyte.docshifter.config.ConfigurationServer;
import com.docbyte.docshifter.config.Constants;
import com.docbyte.docshifter.config.GeneralConfigurationBean;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 6/9/16.
 */
public class DocShifterConfiguration {


	@Bean
	public ConnectionFactory connectionFactory() {

		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(generalConfig().getString(Constants.MQ_URL));
		connectionFactory.setUsername(generalConfig().getString(Constants.MQ_USER));
		connectionFactory.setPassword(generalConfig().getString(Constants.MQ_PASSWORD));
		return connectionFactory;
	}

	@Bean
	public GeneralConfigurationBean generalConfig() {
		return ConfigurationServer.getGeneralConfiguration();
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
		return new Queue(generalConfig().getString(Constants.MQ_QUEUE));

	}
}
