package com.docshifter.core.config;

import com.docshifter.core.config.conditions.IsInContainerCondition;
import com.docshifter.core.config.conditions.IsInKubernetesCondition;
import com.docshifter.core.config.conditions.IsNotInContainerCondition;
import com.docshifter.core.config.services.ConfigurationService;
import com.docshifter.core.config.services.GeneralConfigService;
import com.docshifter.core.config.services.HealthManagementService;
import com.docshifter.core.config.services.IJmsTemplateFactory;
import com.docshifter.core.config.services.JmsTemplateFactory;
import com.docshifter.core.utils.NetworkUtils;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.apache.activemq.artemis.jms.client.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import java.util.List;

/**
 * Created by michiel.vandriessche@docbyte.com on 6/9/16.
 */
@Configuration
@ComponentScan(basePackages = {"com.docshifter.core", "com.docshifter.core.monitoring"})
@EnableDiscoveryClient
@Log4j2
public class DocShifterConfiguration {

	@Value("${queue.replytimeout:300}")
	private int queueReplyTimeout;

	// Default to 5 minutes (5*60*1000)
	@Value("${metrics.timetolive:300000}")
	private long metricsTimeToLive;

	public GeneralConfigService generalConfigService;
	
	public ConfigurationService configurationService;

	private HealthManagementService healthManagementService;

	@Autowired
	public DocShifterConfiguration(GeneralConfigService generalConfigService,
								   ConfigurationService configurationService,
								   HealthManagementService healthManagementService) {
		this.generalConfigService = generalConfigService;
		this.configurationService = configurationService;
		this.healthManagementService = healthManagementService;
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
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public CachingConnectionFactory cachingConnectionFactory() {
		return new CachingConnectionFactory(activeMQConnectionFactory());
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public JmsTemplateFactory jmsTemplateFactory() {
		return new JmsTemplateFactory(cachingConnectionFactory());
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public JmsTemplate defaultJmsTemplate() {
		return jmsTemplateFactory().create(IJmsTemplateFactory.DEFAULT_PRIORITY, queueReplyTimeout,0);
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public JmsTemplate shortLivedJmsTemplate() {
		return jmsTemplateFactory().create(IJmsTemplateFactory.DEFAULT_PRIORITY, queueReplyTimeout, 30_000);
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public JmsTemplate metricsJmsTemplate() {
		JmsTemplate template = jmsTemplateFactory().create(IJmsTemplateFactory.DEFAULT_PRIORITY,
				queueReplyTimeout,metricsTimeToLive);
		return template;
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public JmsTemplate ongoingTaskJmsTemplate() {
		return jmsTemplateFactory().create(IJmsTemplateFactory.HIGHEST_PRIORITY, 0,0);
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	@DependsOn("defaultJmsTemplate")
	public JmsMessagingTemplate defaultMessagingTemplate() {
		return new JmsMessagingTemplate(defaultJmsTemplate());
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	@DependsOn("metricsJmsTemplate")
	public JmsMessagingTemplate metricsMessagingTemplate() {
		return new JmsMessagingTemplate(metricsJmsTemplate());
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	@DependsOn("ongoingTaskJmsTemplate")
	public JmsMessagingTemplate ongoingTaskMessagingTemplate() {
		return new JmsMessagingTemplate(ongoingTaskJmsTemplate());
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	@DependsOn("shortLivedJmsTemplate")
	public JmsMessagingTemplate shortLivedMessagingTemplate() {
		return new JmsMessagingTemplate(shortLivedJmsTemplate());
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public JmsTemplate jmsTemplateMulticast() {
		JmsTemplate template = new JmsTemplate(cachingConnectionFactory());
		template.setPubSubDomain(true);
		template.setPriority(IJmsTemplateFactory.HIGHEST_PRIORITY);
		template.setMessageTimestampEnabled(false);
		return template;
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public JmsListenerContainerFactory<?> jmsListenerContainerFactory(@Qualifier("cachingConnectionFactory") ConnectionFactory connectionFactory,
																	  DefaultJmsListenerContainerFactoryConfigurer configurer) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setErrorHandler(this::handleMQException);
		factory.setExceptionListener(this::handleMQException);
		configurer.configure(factory, connectionFactory);
		return factory;
	}

	private void handleMQException(Throwable t) {
		// Make sure we only freak out if we encounter confirmed unrecoverable errors, as some/most errors with
		// the JMS connection might be perfectly recoverable

		// This one arose sporadically in a NVS PROD environment (DPS-447)
		if (t instanceof javax.jms.IllegalStateException && "Session is closed".equals(t.getMessage())) {
			log.error("Caught an unrecoverable error related to the message queue:", t);
			healthManagementService.reportEvent(HealthManagementService.Event.CRITICAL_MQ_ERROR);
		} else {
			log.warn("Not handling the following message queue error:", t);
		}
	}
	
	/**
	 * Custom JMS listener container to work with topics , this is used in sender to reload configurations
	 * and also in receiver to reload the configuration and clean the cache.
	 * This operations happens when change / save an workflow and console send a notification.
	 * @param connectionFactory auto injected  {@link #cachingConnectionFactory()}
	 * @param configurer The {@link DefaultJmsListenerContainerFactoryConfigurer} to be customized. 
	 */
	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public JmsListenerContainerFactory<?> topicListener(@Qualifier("cachingConnectionFactory") ConnectionFactory connectionFactory,
			DefaultJmsListenerContainerFactoryConfigurer configurer) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		configurer.configure(factory, connectionFactory);		
		// We need to explicit set to true so the listener will work in pub/sub mode.
		factory.setPubSubDomain(true);
		return factory;
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public ActiveMQQueue defaultQueue() {
		return new ActiveMQQueue(generalConfigService.getString(Constants.MQ_QUEUE));
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public ActiveMQQueue defaultMetricsQueue() {
		return new ActiveMQQueue(generalConfigService.getString(Constants.MQ_METRICS_QUEUE));
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public ActiveMQTopic ongoingTaskExchange() {
		return new ActiveMQTopic(Constants.ONGOING_TASK_QUEUE);
	}

	@Bean
	@ConditionalOnMissingClass("com.docshifter.mq.DocshifterMQApplication")
	public ActiveMQTopic reloadExchange() {
		return new ActiveMQTopic(Constants.RELOAD_QUEUE);
	}

	@Bean
	@Conditional(IsInKubernetesCondition.class)
	public KubernetesClient k8sClient() {
		return new DefaultKubernetesClient();
	}

	@Bean
	public MetricsEndpoint metricsEndpoint(MeterRegistry registry) {
		return new MetricsEndpoint(registry);
	}

	@Bean
	public InfoEndpoint infoEndpoint(List<InfoContributor> infoContributors) {
		return new InfoEndpoint(infoContributors);
	}

	@Bean
	public WebClient licensingApiClient() {
		return WebClient.create("https://api.licensing.docshifter.com");
	}

	@Bean
	@Conditional(IsNotInContainerCondition.class)
	public InstallationType classicalInstallationType() {
		return InstallationType.CLASSICAL;
	}

	// If no specific container platform has been detected, fall back to generic
	@Bean
	@Conditional(IsInContainerCondition.class)
	public InstallationType genericContainerInstallationType() {
		return InstallationType.CONTAINERIZED_GENERIC;
	}

	// Otherwise prefer specific container platforms...
	@Bean
	@Primary
	@Conditional(IsInKubernetesCondition.class)
	public InstallationType kubernetesContainerInstallationType() {
		return InstallationType.CONTAINERIZED_KUBERNETES;
	}

	@Bean
	public DataSourceHealthIndicator dataSourceHealthIndicator(DataSource dataSource) {
		return new DataSourceHealthIndicator(dataSource);
	}

	@Bean
	public EurekaInstanceConfigBean eurekaInstanceConfig(InetUtils inetUtils) {
		EurekaInstanceConfigBean bean = new EurekaInstanceConfigBean(inetUtils);
		bean.setHostname(NetworkUtils.getLocalHostName());
		return bean;
	}
}
