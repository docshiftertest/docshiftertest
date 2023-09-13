package com.docshifter.core.config.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@Entity
@NoArgsConstructor
public class Properties implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column
    private String key;

    @Column
    private String value;

    @Column
    private String description;

    @Column
    private String label;

    @Column
    private String application;

    @Column
    private String profile;

    @Column
    private boolean enabled;

}
