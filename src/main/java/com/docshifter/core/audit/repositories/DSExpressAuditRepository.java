package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.DSExpressAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DSExpressAuditRepository extends JpaRepository<DSExpressAudit, String> {

    @Query(
            value = "select * from audit.dsexpress_audit da where da.event_date_time between :startTime and :endTime and (  'ALL' in (:authorList) or da.username in ( :authorList ))",
            nativeQuery = true
    )
    List<DSExpressAudit> findAllChangesByDateAuthor(@Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("authorList") List<String> authorList);

    @Query(
            value = "SELECT distinct da.username FROM audit.dsexpress_audit wa where da.event_date_time between :startTime and :endTime ",
            nativeQuery = true
    )
    List<String> findAllUsername(@Param("startTime") Long startTime, @Param("endTime") Long endTime);
}
