package com.docshifter.core.config;

import com.docshifter.core.config.services.GeneralConfigService;
import com.docshifter.core.utils.RoundRobinList;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompReactorNettyCodec;
import org.springframework.messaging.tcp.reactor.ReactorNettyTcpClient;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnMissingClass("com.docshifter.console.DocShifterConsole")
@EnableWebSocketMessageBroker
@Log4j2
public class WebSocketConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    private final GeneralConfigService generalConfigService;

    public WebSocketConfig(GeneralConfigService generalConfigService) {
        this.generalConfigService = generalConfigService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        String mqUser = generalConfigService.getString(Constants.MQ_USER);
        String mqPassword = generalConfigService.getString(Constants.MQ_PASSWORD);
        String savedMqUrl = generalConfigService.getString(Constants.MQ_URL);

        // we are in HA mode
        if (savedMqUrl.contains("ha=true")) {
            config.enableStompBrokerRelay("/queue", "/topic", "/user")
                    .setTcpClient(createTcpClient(savedMqUrl))
                    .setUserDestinationBroadcast("/topic/log-unresolved-user")
                    .setUserRegistryBroadcast("/topic/log-user-registry")
                    .setClientLogin(mqUser)
                    .setClientPasscode(mqPassword)
                    .setSystemLogin(mqUser)
                    .setSystemPasscode(mqPassword);
            config.setApplicationDestinationPrefixes("/app");
        } else {
            var mqUrl = buildMQURI(savedMqUrl);
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
    }

    /**
     * Builds URI from the MQ URL
     * @param mqUrl the saved MQ URL
     * @return MQ url as URI
     */
    private URI buildMQURI(String mqUrl) {
        try {
            return new URI(mqUrl);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Malformed MQ URL: " + mqUrl, ex);
        }
    }

    /**
     * Handle MQ URL when HA mode
     * @param savedMQUrl the MQ URL ( (tcp://10.110.0.74:61616,tcp://3.249.114.10:61616)?ha=true )
     * @return new Tcp Client with RoundRobin callback
     */
    private ReactorNettyTcpClient<byte[]> createTcpClient(String savedMQUrl) {
        // Removes irregular chars keeping only "/ : and ."
        // input (tcp://10.110.0.74:61616,tcp://3.249.114.10:61616)?ha=true
        // output tcp://10.110.0.74:61616 and tcp://3.249.114.10:61616
        var mqUrlAddresses = Arrays.stream(savedMQUrl.replaceAll("[^a-zA-Z0-9/:.,-]", "")
                        .replace("hatrue", "")
                        // Split urls by , before building the URI
                        .split(","))
                .map(this::buildMQURI)
                .map(item -> new InetSocketAddress(item.getHost(), Integer.parseInt("61613")))
                .toList();

        final var addresses = new RoundRobinList<>(mqUrlAddresses);

        return new ReactorNettyTcpClient<>(
                client -> client.remoteAddress(() -> addresses.iterator().next()),
                new StompReactorNettyCodec());
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
