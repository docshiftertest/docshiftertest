package com.docshifter.core.audit.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private String details;

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

}
