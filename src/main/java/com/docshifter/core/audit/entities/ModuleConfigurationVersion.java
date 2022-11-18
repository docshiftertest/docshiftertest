package com.docshifter.core.audit.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ModuleConfigurationVersion {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private UUID moduleConfigurationUuid;

    private String moduleConfigurationName;

    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(columnDefinition = "jsonb")
    private String moduleConfigurationJson;

    private Integer version;
}
