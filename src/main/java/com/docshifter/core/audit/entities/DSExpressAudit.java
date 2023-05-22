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
public class DSExpressAudit implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    private String username;
    private long eventDateTime;
    private String dsexpressName;
    private String propertyName;
    private String oldValue;
    private String newValue;
    private String changeType;

    public DSExpressAudit(UUID id,
                          String dsexpressName,
                          String propertyName,
                          String changeType,
                          String oldValue,
                          String newValue,
                          String username,
                          long eventDateTime) {
        this.id = id;
        this.propertyName = propertyName;
        this.dsexpressName = dsexpressName;
        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.username = username;
        this.eventDateTime = eventDateTime;
    }

}
