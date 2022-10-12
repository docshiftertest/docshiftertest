package com.docshifter.core.audit.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class WorkflowAudit implements AuditInfo {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String username;
    private long eventDateTime;
    private String workflowName;

    private String configurationName;
    private String propertyName;
    private String oldValue;
    private String newValue;
    private String changeType;
    private String moduleName;

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public long getEventDateTime() {
        return this.eventDateTime;
    }

    @Override
    public void setEventDateTime(long eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

}
