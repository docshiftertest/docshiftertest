package com.docshifter.core.monitoring.entities;

import com.docshifter.core.monitoring.entities.Configuration;
import com.docshifter.core.monitoring.enums.NotificationLevels;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by blazejm on 19.05.2017.
 */
@Entity(name = "MonitoringConfigItem")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type")
public abstract class AbstractConfigurationItem {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="configurationId", nullable = false)
    private Configuration configuration;

    @ElementCollection(targetClass = NotificationLevels.class)
    @JoinTable(name = "MonitoringNotificationLevels", joinColumns = @JoinColumn(name = "configurationItemId"))
    @Column(name = "level", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<NotificationLevels> notificationLevels;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Set<NotificationLevels> getNotificationLevels() {
        return notificationLevels;
    }

    public void setNotificationLevels(Set<NotificationLevels> notificationLevels) {
        this.notificationLevels = notificationLevels;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
