package com.docshifter.core.monitoring.services;

import com.docshifter.core.monitoring.dtos.NotificationDto;
import com.docshifter.core.monitoring.dtos.SnmpConfigurationItemDto;
import com.docshifter.core.monitoring.services.SnmpNotificationService;
import org.apache.log4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by blazejm on 18.05.2017.
 */
@Service
public class SnmpNotificationServiceImpl implements SnmpNotificationService {
    private static final Logger log = Logger.getLogger(com.docshifter.core.monitoring.services.SnmpNotificationServiceImpl.class.getName());

    @Override
    public void sendNotification(SnmpConfigurationItemDto snmpConfigurationItem, NotificationDto notification) {
        try {
            //Create Transport Mapping
            TransportMapping transport = new DefaultUdpTransportMapping();
            transport.listen();

            //Create Target
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(snmpConfigurationItem.getCommunity()));
            target.setVersion(SnmpConstants.version2c);
            target.setAddress(new UdpAddress(snmpConfigurationItem.getIpAddress() + "/" + snmpConfigurationItem.getPort()));
            target.setRetries(2);
            target.setTimeout(5000);

            //Create PDU for V2
            PDU pdu = new PDU();

            // need to specify the system up time
            pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(new Date().toString())));
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(snmpConfigurationItem.getTrapOid())));
            pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress, new IpAddress(snmpConfigurationItem.getIpAddress()))); //TODO check if it is agent or manager address

            // variable binding for Enterprise Specific objects, Severity (should be defined in MIB file) TODO check what is MIB file
            pdu.add(new VariableBinding(new OID(snmpConfigurationItem.getTrapOid()), new OctetString(notification.getMessage())));

            pdu.setType(PDU.NOTIFICATION);

            //Send the PDU
            Snmp snmp = new Snmp(transport);
            System.out.println("Sending V2 Trap to " + snmpConfigurationItem.getIpAddress() + " on Port " + snmpConfigurationItem.getPort());
            snmp.send(pdu, target);
            snmp.close();

            log.info("sendNotification to: " + snmpConfigurationItem.getIpAddress()
                    + " with message: " + notification.getMessage() + " successful");
        } catch (Exception ex) {
            log.error("Unknown exception: %s".formatted(ex.getMessage()), ex);
            ex.printStackTrace();
        }
    }
}
