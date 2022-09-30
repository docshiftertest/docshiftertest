package com.docshifter.core.audit.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AccessAudit extends DefaultAuditInfo {

    private String details;
}
