package com.docshifter.core.config;

import com.docshifter.core.config.services.GeneralConfigService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
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

		config.enableStompBrokerRelay("/queue", "/topic", "/user")
				.setRelayHost(mqUrl.getHost())
				.setRelayPort(61613)
				.setUserDestinationBroadcast("/topic/log-unresolved-user")
				.setUserRegistryBroadcast("/topic/log-user-registry")
				.setClientLogin(mqUser)
				.setClientPasscode(mqPassword)
				.setSystemLogin(mqUser)
				.setSystemPasscode(mqPassword);
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/" + Constants.API_PATH_PREFIX + "/stomp").setAllowedOrigins("*");
	}

	@Override
	public void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
		// We are required to register STOMP endpoints even if we just want to publish messages and don't want to serve
		// an actual Websocket endpoint on any components other than Console, otherwise Spring will complain. However,
		// we can deny all traffic by default, and then override and set up the correct inbound traffic rules in
		// Console.
		messages.anyMessage().denyAll();
	}
}
