package com.docshifter.core.audit.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationAudit implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    private String oldValue;
    private String newValue;
    private String username;
    private long eventDateTime;
    private String eventType;
    private String eventAction;
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(columnDefinition = "jsonb")
    private Object details;
}
