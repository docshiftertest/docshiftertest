package com.docshifter.core;

import com.docshifter.core.utils.RoundRobinList;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@Log4j2
public class RoundRobinListTest {

    @Test
    public void cycleBuildURI() {
        var savedMqUrl = "(tcp://10.110.0.74:61616,tcp://3.249.114.10:61616)?ha=true";

        var mqUrlAddresses = Arrays.stream(savedMqUrl.replaceAll("[^a-zA-Z0-9/:.,]", "")
                        .replace("hatrue", "")
                        .split(","))
                .map(this::buildMQURI)
                .map(item -> new InetSocketAddress(item.getHost(), item.getPort()))
                .toList();

        var list = new RoundRobinList<>(mqUrlAddresses);

        for (int i = 0; i < 6; i++) {
            var next = list.iterator().next();
            log.info(next);
            Assert.assertTrue(mqUrlAddresses.contains(next));
        }
    }

    private URI buildMQURI(String mqUrl) {
        try {
            return new URI(mqUrl);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Malformed MQ URL: " + mqUrl, ex);
        }
    }
}
