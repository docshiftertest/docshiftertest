package com.docshifter.core.monitoring.entities;

import java.util.Set;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.docshifter.core.monitoring.enums.NotificationLevels;

/**
 * Created by blazejm on 19.05.2017.
 */
@Entity(name = "MonitoringConfigItem")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class AbstractConfigurationItem {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="configurationId", nullable = false)
    private Configuration configuration;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToOne(mappedBy = "configurationItem", cascade = CascadeType.ALL)
    @JoinColumn(name="monitoring_config_item_id", nullable = false)
    private MonitoringFilter monitoringFilter;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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

    public MonitoringFilter getMonitoringFilter() {
        return monitoringFilter;
    }

    public void setMonitoringFilter(MonitoringFilter monitoringFilter) {
        this.monitoringFilter = monitoringFilter;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
