package com.docshifter.core.monitoring.entities;

import com.docshifter.core.monitoring.enums.ConfigurationTypes;
import org.javers.core.metamodel.annotation.TypeName;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * Created by blazejm on 16.05.2017.
 */
@Entity(name = "MonitoringWebhookConfigItem")
@DiscriminatorValue(ConfigurationTypes.WEBHOOK)
@TypeName("MonitoringSnmpConfigItem")
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
