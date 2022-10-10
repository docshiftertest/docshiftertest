package com.docshifter.core.monitoring.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by blazejm on 17.05.2017.
 */
@Entity(name = "MonitoringMailTemplate")
public class MailTemplate {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String taskId;
    @Column(length = 998)
    private String title;
    @Column(length = 1000)
    private String body;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="mailConfigurationItemId", nullable = false)
    private MailConfigurationItem mailConfigurationItem;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public MailConfigurationItem getMailConfigurationItem() {
        return mailConfigurationItem;
    }

    public void setMailConfigurationItem(MailConfigurationItem mailConfigurationItem) {
        this.mailConfigurationItem = mailConfigurationItem;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
