package com.docshifter.core.audit.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractAuditInfo {


    private String oldValue;
    private String newValue;
    private String username;
    private long eventDateTime;
    private String propertyName;

}
