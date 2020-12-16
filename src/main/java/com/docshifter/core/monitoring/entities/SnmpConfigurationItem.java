package com.docshifter.core.monitoring.entities;

import com.docshifter.core.monitoring.entities.AbstractConfigurationItem;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by blazejm on 18.05.2017.
 */
@Entity(name = "MonitoringSnmpConfigItem")
@DiscriminatorValue(ConfigurationTypes.SNMP)
public class SnmpConfigurationItem extends AbstractConfigurationItem {
    private String community = "public";
    private String trapOid = ".1.3.6.1.2.1.1.6";
    private String ipAddress = "127.0.0.1";
    private int port = 163;

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getTrapOid() {
        return trapOid;
    }

    public void setTrapOid(String trapOid) {
        this.trapOid = trapOid;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
