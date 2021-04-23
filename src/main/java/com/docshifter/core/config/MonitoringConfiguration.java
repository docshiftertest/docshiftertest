package com.docshifter.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by blazejm on 30.05.2017.
 */
@Configuration
public class MonitoringConfiguration {
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * ExecutorService that dynamically spawns and reuses threads as needed, recommended for general-purpose tasks.
	 */
	@Bean(name = "generalES")
	@Primary
	public ExecutorService executorService() {
		return Executors.newCachedThreadPool();
	}

	/**
	 * ExecutorService that supports scheduling backed by a fixed number of threads, exclusively for lightweight and
	 * pure CPU-bound tasks.
	 */
	@Bean(name = "lightweightScheduledES")
	public ScheduledExecutorService scheduledExecutorService() {
		return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	}

}
