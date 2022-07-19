package com.docshifter.core.monitoring.entities;

import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

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
