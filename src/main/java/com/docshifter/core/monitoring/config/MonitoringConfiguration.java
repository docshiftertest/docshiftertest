package com.docshifter.core.monitoring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by blazejm on 30.05.2017.
 */
@Configuration
public class MonitoringConfiguration {
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate;
	}

	@Bean
	public ExecutorService executorService() {
		ExecutorService executor = Executors.newCachedThreadPool();
		return executor;
	}

}
