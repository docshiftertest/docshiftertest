package com.docshifter.core.config;

public class Constants {

	public final static String MQ_SYSTEM = "mq_system";
	public final static String MQ_URL = "mq_url";
	public final static String MQ_QUEUE = "mq_queue";
	public final static String MQ_USER = "mq_user";
	public final static String MQ_PASSWORD = "mq_password";
	public final static String TEMPFOLDER = "tempfolder";
	public final static String ERRORFOLDER = "errorfolder";
	public final static String OPENOFFICE_HOST = " ";
	public final static String OPENOFFICE_PORT = " ";


    public final static String RELOAD_QUEUE = "VirtualTopic.docshifterReload";
	public final static String SYNC_QUEUE = "docshifterSync";
	
	public final static String DEFAULT_QUEUE = "docshifter";
	
	/**
	 * Used to cache {@link com.docshifter.core.config.services.ConfigurationService#getSenderConfiguration(long)}
	 */
	public final static String SENDER_CONFIGURATION_CACHE = "senderConfiguration";
	
	/**
	 * This constant should reflect 
	 * {@link DocShifterConfiguration#topicListener(javax.jms.ConnectionFactory, org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer)} method name.
	 */
	public final static String TOPIC_LISTENER = "topicListener";
}
