package com.docshifter.core.monitoring.entities;

import com.docshifter.core.monitoring.entities.AbstractConfigurationItem;
import com.docshifter.core.monitoring.entities.MailTemplate;
import com.docshifter.core.monitoring.entities.SmtpConfiguration;
import com.docshifter.core.monitoring.enums.ConfigurationTypes;

import javax.persistence.*;
import java.util.List;

/**
 * Created by blazejm on 11.05.2017.
 */
@Entity(name = "MonitoringMailConfigItem")
@DiscriminatorValue(ConfigurationTypes.MAIL)
@Table(schema = "DOCSHIFTER", name="MONITORING_MAIL_CONFIG_ITEM")
public class MailConfigurationItem extends AbstractConfigurationItem {
    @OneToOne(cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    private SmtpConfiguration smtpConfiguration;

    @ElementCollection
    @JoinTable(name = "MonitoringMailAddress", joinColumns = @JoinColumn(name = "mailConfigurationItemId"))
    @Column(name = "email", length=320, nullable = false)
    private List<String> mailAddresses;

    @OneToMany(mappedBy="mailConfigurationItem",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<MailTemplate> mailTemplates;

    public SmtpConfiguration getSmtpConfiguration() {
        return smtpConfiguration;
    }

    public void setSmtpConfiguration(SmtpConfiguration smtpConfiguration) {
        this.smtpConfiguration = smtpConfiguration;
    }

    public List<String> getMailAddresses() {
        return mailAddresses;
    }

    public void setMailAddresses(List<String> mailAddresses) {
        this.mailAddresses = mailAddresses;
    }

    public List<MailTemplate> getMailTemplates() {
        return mailTemplates;
    }

    public void setMailTemplates(List<MailTemplate> mailTemplates) {
        this.mailTemplates = mailTemplates;
    }
}
