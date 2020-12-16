package com.docshifter.core.monitoring.snmp;

import org.apache.log4j.Logger;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.test.MultiThreadedTrapReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blazejm on 22.05.2017.
 */
public class SnmpTrapReceiver extends MultiThreadedTrapReceiver {
    private static final Logger log = Logger.getLogger(SnmpTrapReceiver.class.getName());

    private List<CommandResponderEvent> eventList = new ArrayList<>();

    public static void main(String[] args) {
        SnmpTrapReceiver snmpTrapReceiver = new SnmpTrapReceiver();
        snmpTrapReceiver.run();
    }

    public List<CommandResponderEvent> getEventList() {
        return eventList;
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        super.processPdu(event);
        log.info("Received SNMP trap: " + toString(event));
        eventList.add(event);
    }

    private static String toString(CommandResponderEvent event) {
        StringBuilder result = new StringBuilder();
        event.getPDU().getVariableBindings()
                .forEach(var -> result.append(var.toString()).append("; "));
        return result.toString();
    }

}
