package com.docshifter.core.monitoring.entities;

import com.docshifter.core.security.Encrypted;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Created by blazejm on 29.09.2017.
 */
@Entity(name = "MonitoringDbConfigItem")
@DiscriminatorValue(ConfigurationTypes.DB)
public class DbConfigurationItem extends AbstractConfigurationItem {
    private String driver;
    private String connection;
    private String user;
    @Encrypted
    private String password;
    private String tableName;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
