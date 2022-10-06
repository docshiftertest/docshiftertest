package com.docshifter.core.config.entities;

import com.docshifter.core.security.Encrypted;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class GlobalNotification {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    private String notificationEmailFrom;
    private String notificationEmailTo;
    private String notificationEmailUsername;
    @Encrypted
    private String notificationEmailPassword;
    private String notificationHost;
    private String notificationPort;
    private String notificationSecurityOption;

    private String licenseDaysBeforeExpire;
    private String licenseTimeInterval;

    private String lastDateNotificationCheck;

    public GlobalNotification() {}

    public GlobalNotification(long id,
                              String notificationEmailFrom,
                              String notificationEmailTo,
                              String notificationEmailUsername,
                              String notificationEmailPassword,
                              String notificationHost,
                              String notificationPort,
                              String notificationSecurityOption,
                              String licenseDaysBeforeExpire,
                              String licenseTimeInterval,
                              String lastDateNotificationCheck) {
        this.id = id;
        this.notificationEmailFrom = notificationEmailFrom;
        this.notificationEmailTo = notificationEmailTo;
        this.notificationEmailUsername = notificationEmailUsername;
        this.notificationEmailPassword = notificationEmailPassword;
        this.notificationHost = notificationHost;
        this.notificationPort = notificationPort;
        this.notificationSecurityOption = notificationSecurityOption;
        this.licenseDaysBeforeExpire = licenseDaysBeforeExpire;
        this.licenseTimeInterval = licenseTimeInterval;
        this.lastDateNotificationCheck = lastDateNotificationCheck;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNotificationEmailFrom() {
        return notificationEmailFrom;
    }

    public void setNotificationEmailFrom(String notificationEmailFrom) {
        this.notificationEmailFrom = notificationEmailFrom;
    }

    public String getNotificationEmailTo() {
        return notificationEmailTo;
    }

    public void setNotificationEmailTo(String notificationEmailTo) {
        this.notificationEmailTo = notificationEmailTo;
    }

    public String getNotificationEmailUsername() {
        return notificationEmailUsername;
    }

    public void setNotificationEmailUsername(String notificationEmailUsername) {
        this.notificationEmailUsername = notificationEmailUsername;
    }

    public String getNotificationEmailPassword() {
        return notificationEmailPassword;
    }

    public void setNotificationEmailPassword(String notificationEmailPassword) {
        this.notificationEmailPassword = notificationEmailPassword;
    }

    public String getNotificationHost() {
        return notificationHost;
    }

    public void setNotificationHost(String notificationHost) {
        this.notificationHost = notificationHost;
    }

    public String getNotificationPort() {
        return notificationPort;
    }

    public void setNotificationPort(String notificationPort) {
        this.notificationPort = notificationPort;
    }

    public String getNotificationSecurityOption() {
        return notificationSecurityOption;
    }

    public void setNotificationSecurityOption(String notificationSecurityOption) {
        this.notificationSecurityOption = notificationSecurityOption;
    }

    public String getLicenseDaysBeforeExpire() {
        return licenseDaysBeforeExpire;
    }

    public void setLicenseDaysBeforeExpire(String licenseNotificationDaysBeforeExpire) {
        this.licenseDaysBeforeExpire = licenseNotificationDaysBeforeExpire;
    }

    public String getLicenseTimeInterval() {
        return licenseTimeInterval;
    }

    public void setLicenseTimeInterval(String licenseTimeInterval) {
        this.licenseTimeInterval = licenseTimeInterval;
    }

    public String getLastDateNotificationCheck() {
        return lastDateNotificationCheck;
    }

    public void setLastDateNotificationCheck(String lastDateNotificationCheck) {
        this.lastDateNotificationCheck = lastDateNotificationCheck;
    }

}
