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
public class ApplicationAudit implements AuditInfo {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String oldValue;
    private String newValue;
    private String username;
    private long eventDateTime;
    private String propertyName;

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

    @Override
    public String getOldValue() {
        return oldValue;
    }

    @Override
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    @Override
    public String getNewValue() {
        return newValue;
    }

    @Override
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
