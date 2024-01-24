package com.docshifter.core.monitoring.entities;

import com.docshifter.core.monitoring.enums.ConfigurationTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.List;

/**
 * Created by blazejm on 16.05.2017.
 */
@Entity(name = "MonitoringWebhookConfigItem")
@DiscriminatorValue(ConfigurationTypes.WEBHOOK)
public class WebhookConfigurationItem extends AbstractConfigurationItem {
    private String url;

    @OneToMany(mappedBy="webhookConfigurationItem",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<WebhookTemplate> webhookTemplates;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<WebhookTemplate> getWebhookTemplates() {
        return webhookTemplates;
    }

    public void setWebhookTemplates(List<WebhookTemplate> webhookTemplates) {
        this.webhookTemplates = webhookTemplates;
    }
}
