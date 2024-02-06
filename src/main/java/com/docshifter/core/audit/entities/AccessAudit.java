package com.docshifter.core.audit.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class AccessAudit{

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    private String username;
    private long eventDateTime;
    private String details;
    private String oldValue;
    private String newValue;

    public AccessAudit(UUID id, String username, long eventDateTime, String details) {
        this.id = id;
        this.username = username;
        this.eventDateTime = eventDateTime;
        this.details = details;
    }
}
