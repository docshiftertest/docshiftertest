package com.docshifter.core.audit.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
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
}
