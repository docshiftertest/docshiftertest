package com.docshifter.core.monitoring.entities;

import com.docshifter.core.monitoring.enums.ConfigurationTypes;
import org.javers.core.metamodel.annotation.TypeName;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.List;

/**
 * Created by blazejm on 11.05.2017.
 */
@Entity(name = "MonitoringMailConfigItem")
@DiscriminatorValue(ConfigurationTypes.MAIL)
@TypeName("MonitoringMailConfigItem")
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
