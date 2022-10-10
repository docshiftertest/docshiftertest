package com.docshifter.core.audit.entities;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class WorkflowAudit extends AbstractAuditInfo {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String workflowName;
    private String changeType;
    private String configurationName;
    private String moduleName;

    public WorkflowAudit(UUID uuid, String workflowName, String changeType, String propertyName, String moduleName, String oldValue, String newValue,
            String configurationName , String username, long eventDateTime){
        this.id = uuid;
        this.workflowName = workflowName;
        this.setEventDateTime(eventDateTime);
        this.changeType = changeType;
        this.configurationName = configurationName;
        this.moduleName = moduleName;
        this.setOldValue(oldValue);
        this.setNewValue(newValue);
        this.setPropertyName(propertyName);
        this.setUsername(username);

    }

}
