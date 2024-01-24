package com.docshifter.core.config.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * @author Juan Marques
 * @created 11/08/2023
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionBased {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int tokenAmount;

    @Version
    private Long version;

    public void incrementTokenAmount(int amount) {
        this.tokenAmount += amount;
    }
}
