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
public class ApplicationAudit extends AbstractAuditInfo {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String details;

    public ApplicationAudit(UUID id, String details, String oldValue, String newValue, String propertyName, String username, long eventDateTime){
        this.id = id;
        this.details = details;
        this.setOldValue(oldValue);
        this.setNewValue(newValue);
        this.setPropertyName(propertyName);
        this.setEventDateTime(eventDateTime);
        this.setUsername(username);
    }
}
