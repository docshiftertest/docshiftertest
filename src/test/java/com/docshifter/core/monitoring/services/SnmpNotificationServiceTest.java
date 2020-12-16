package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.AbstractSpringTest;
import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.dtos.SnmpConfigurationItemDto;
import com.docshifter.core.monitoring.enums.NotificationLevels;
import com.docshifter.core.monitoring.services.SnmpNotificationService;
import com.docshifter.core.monitoring.snmp.SnmpTrapReceiver;
import org.junit.Before;
import org.junit.Test;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SocketUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by blazejm on 29.05.2017.
 */
public class SnmpNotificationServiceTest extends AbstractSpringTest {
    @Autowired
    private SnmpNotificationService snmpNotificationService;

    private SnmpTrapReceiver snmpTrapReceiver;

    private SnmpConfigurationItemDto snmpConfigurationItem;


    @Before
    public void beforeTest() {
        int port = SocketUtils.findAvailableUdpPort();
        System.setProperty("snmp4j.listenAddress", "udp:0.0.0.0/" + port);

        snmpTrapReceiver = new SnmpTrapReceiver();
        snmpTrapReceiver.run();

        snmpConfigurationItem = new SnmpConfigurationItemDto();
        snmpConfigurationItem.setPort(port);
    }

    @Test
    public void shouldInjectService() {
        assertThat(snmpNotificationService).isNotNull();
    }

    @Test
    public void shouldSendNotification() throws InterruptedException {
        NotificationDto notification = new NotificationDto();
        notification.setLevel(NotificationLevels.ERROR);
        notification.setMessage("some body");

        snmpNotificationService.sendNotification(snmpConfigurationItem, notification);
        TimeUnit.SECONDS.sleep(1);

        assertThat(snmpTrapReceiver.getEventList()).isNotEmpty();
        CommandResponderEvent event = snmpTrapReceiver.getEventList().get(0);
        assertThat(event.getPDU()).isNotNull();
        VariableBinding eventTrapValue = event.getPDU()
                .getVariableBindings()
                .stream()
                .filter(v -> v.getOid().equals(new OID(snmpConfigurationItem.getTrapOid())))
                .findAny()
                .orElse(null);
        assertThat(eventTrapValue).isNotNull();
        assertThat(eventTrapValue.getVariable().toString()).isEqualTo("some body");
    }

}
