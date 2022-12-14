package com.docshifter.core.config.entities;

import com.docshifter.core.security.Encrypted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class GlobalNotification {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    private String notificationTenantId;
    private String notificationClientId;
    @Encrypted
    private String notificationClientSecret;

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
}
