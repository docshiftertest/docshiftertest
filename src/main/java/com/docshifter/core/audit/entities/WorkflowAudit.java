package com.docshifter.core.audit.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class WorkflowAudit implements Serializable {

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

    public WorkflowAudit(UUID uuid, String workflowName, String changeType, String propertyName, String moduleName, String oldValue, String newValue,
            String configurationName , String username, long eventDateTime) {
        this.id = uuid;
        this.workflowName = workflowName;
        this.eventDateTime = eventDateTime;
        this.changeType = changeType;
        this.configurationName = configurationName;
        this.moduleName = moduleName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.propertyName = propertyName;
        this.username = username;
    }

}
