package com.docshifter.core.audit.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String moduleConfigurationJson;

    private Integer version;
}
