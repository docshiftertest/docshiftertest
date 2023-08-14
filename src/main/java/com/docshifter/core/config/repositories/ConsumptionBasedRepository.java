package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.ConsumptionBased;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Juan Marques
 * @created 11/08/2023
 */
@Repository
public interface ConsumptionBasedRepository extends JpaRepository<ConsumptionBased, Long> {
}
