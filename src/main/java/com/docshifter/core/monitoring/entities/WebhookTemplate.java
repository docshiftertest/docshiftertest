package com.docshifter.core.monitoring.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import java.util.Map;

/**
 * Created by blazejm on 17.05.2017.
 */
@Entity(name = "MonitoringWebhookTemplate")
public class WebhookTemplate {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String taskId;
    @Column(length=1000)
    private String body;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="webhookConfigurationItemId", nullable = false)
    private WebhookConfigurationItem webhookConfigurationItem;

    @ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value", length=1000)
    @CollectionTable(name="MonitoringWebhookUrlParams", joinColumns=@JoinColumn(name="webhookTemplateId"))
    private Map<String, String> urlParams;

    @ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value", length=1000)
    @CollectionTable(name="MonitoringWebhookHeaderParams", joinColumns=@JoinColumn(name="webhookTemplateId"))
    private Map<String, String> headerParams;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public WebhookConfigurationItem getWebhookConfigurationItem() {
        return webhookConfigurationItem;
    }

    public void setWebhookConfigurationItem(WebhookConfigurationItem webhookConfigurationItem) {
        this.webhookConfigurationItem = webhookConfigurationItem;
    }

    public Map<String, String> getUrlParams() {
        return urlParams;
    }

    public void setUrlParams(Map<String, String> urlParams) {
        this.urlParams = urlParams;
    }

    public Map<String, String> getHeaderParams() {
        return headerParams;
    }

    public void setHeaderParams(Map<String, String> headerParams) {
        this.headerParams = headerParams;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
