package com.docshifter.core.config.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.TypeName;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

//@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Cacheable
@TypeName("LoginEntity")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;
    private String username;
}
