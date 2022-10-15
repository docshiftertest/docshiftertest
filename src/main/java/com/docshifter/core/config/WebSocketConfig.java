package com.docshifter.core.config;

import com.docshifter.core.config.services.GeneralConfigService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.StompBrokerRelayRegistration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@ConditionalOnMissingClass("com.docshifter.console.DocShifterConsole")
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
	private final GeneralConfigService generalConfigService;

	public WebSocketConfig(GeneralConfigService generalConfigService) {
		this.generalConfigService = generalConfigService;
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		URI mqUrl;
		try {
			mqUrl = new URI(generalConfigService.getString(Constants.MQ_URL));
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException("Malformed MQ URL: " + generalConfigService.getString(Constants.MQ_URL), ex);
		}
		String mqUser = generalConfigService.getString(Constants.MQ_USER);
		String mqPassword = generalConfigService.getString(Constants.MQ_PASSWORD);

		StompBrokerRelayRegistration relayConfig = config.enableStompBrokerRelay("/queue", "/topic")
				.setRelayHost(mqUrl.getHost())
				.setUserDestinationBroadcast("/topic/log-unresolved-user")
				.setUserRegistryBroadcast("/topic/log-user-registry")
				.setClientLogin(mqUser)
				.setClientPasscode(mqPassword)
				.setSystemLogin(mqUser)
				.setSystemPasscode(mqPassword);
		relayConfig.setRelayPort(61613);
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/" + Constants.API_PATH_PREFIX + "/stomp").setAllowedOrigins("*");
	}

	@Override
	public void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
		messages.anyMessage().denyAll();
	}
}
