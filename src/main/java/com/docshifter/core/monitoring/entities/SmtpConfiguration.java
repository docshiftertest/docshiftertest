package com.docshifter.core.monitoring.entities;

import com.docshifter.core.security.Encrypted;

import javax.persistence.*;

/**
 * Created by blazejm on 12.05.2017.
 */
@Entity(name = "MonitoringSmtpConfig")
@Table(schema = "DOCSHIFTER")
public class SmtpConfiguration {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String host;
    private int port;
    @Column(length=320)
    private String username;
    @Encrypted
    private String password;
    @Column(length=320)
    private String fromAddress;
    private boolean ssl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }
}
