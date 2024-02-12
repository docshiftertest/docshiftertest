package com.docshifter.core.monitoring.entities;

import com.docshifter.core.monitoring.enums.ConfigurationTypes;
import com.docshifter.core.security.Encrypted;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Created by blazejm on 29.09.2017.
 */
@Entity(name = "MonitoringDbConfigItem")
@DiscriminatorValue(ConfigurationTypes.DB)
public class DbConfigurationItem extends AbstractConfigurationItem {
    private String driver;
    private String connection;
    private String dbUser;
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

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
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
