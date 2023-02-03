package com.docshifter.core.config;

public final class Constants {
	private Constants() {}

	public static final String MQ_SYSTEM = "mq_system";
	public static final String MQ_URL = "mq_url";
	public static final String MQ_QUEUE = "mq_queue";
	public static final String MQ_METRICS_QUEUE = "docshifter_metrics";
	public static final String MQ_USER = "mq_user";
	public static final String MQ_PASSWORD = "mq_password";
	public static final String TEMPFOLDER = "tempfolder";
	public static final String ERRORFOLDER = "errorfolder";
	public static final String OPENOFFICE_HOST = " ";
	public static final String OPENOFFICE_PORT = " ";


    public static final String RELOAD_QUEUE = "VirtualTopic.docshifterReload";
	public static final String SYNC_QUEUE = "docshifterSync";
	public static final String DEFAULT_QUEUE = "docshifter";

	public static final String STOMP_ONGOING_TASK_DESTINATION = "/topic/ongoingTask";
	public static final String ONGOING_TASK_QUEUE_DESTINATION = "VirtualTopic.ongoingTask";
	
	/**
	 * Used to cache {@link com.docshifter.core.config.services.ConfigurationService#getSenderConfiguration(long)}
	 */
	public static final String SENDER_CONFIGURATION_CACHE = "senderConfiguration";
	
	/**
	 * This constant should reflect 
	 * {@link DocShifterConfiguration#topicListener(javax.jms.ConnectionFactory, org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer)} method name.
	 */
	public static final String TOPIC_LISTENER = "topicListener";

	/**
	 * Prefix to use for all API routes
	 */
	public static final String API_PATH_PREFIX = "api";
}
