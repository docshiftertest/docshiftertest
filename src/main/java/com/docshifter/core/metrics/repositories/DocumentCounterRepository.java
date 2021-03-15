package com.docshifter.core.metrics.repositories;

import com.docshifter.core.metrics.dtos.DocumentCounterDTO;
import com.docshifter.core.metrics.entities.DocumentCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public interface DocumentCounterRepository extends JpaRepository<DocumentCounter, String> {

    @Query("SELECT SUM(c.counts) FROM DocumentCounter c")
    int selectTotalCounts();

}
