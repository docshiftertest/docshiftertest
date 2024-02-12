package com.docshifter.core.config.services;

import com.netflix.discovery.EurekaClient;
import org.springframework.context.SmartLifecycle;

public abstract class EurekaAware implements SmartLifecycle {
	private boolean started;
	protected final EurekaClient eurekaClient;

	protected EurekaAware(EurekaClient eurekaClient) {
		this.eurekaClient = eurekaClient;
	}

	/**
	 * Do not use the {@link EurekaClient} in a {@link jakarta.annotation.PostConstruct} method or in a
	 * {@link org.springframework.scheduling.annotation.Scheduled} method (or anywhere where the
	 * {@link org.springframework.context.ApplicationContext} might not be started yet). It is initialized in a
	 * {@link SmartLifecycle} (with {@code phase=0}), so the earliest you can rely on it being available is in
	 * another {@link SmartLifecycle} with a higher phase.
	 * @see <a href="https://cloud.spring.io/spring-cloud-netflix/multi/multi__service_discovery_eureka_clients.html#_using_the_eurekaclient">The official Spring Cloud documentation</a>
	 */
	@Override
	public int getPhase() {
		return 1;
	}

	@Override
	public void start() {
		started = true;
	}

	@Override
	public void stop() {
		started = false;
	}

	@Override
	public boolean isRunning() {
		return started;
	}
}
