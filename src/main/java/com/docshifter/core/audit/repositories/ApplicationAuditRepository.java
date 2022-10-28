package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.ApplicationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationAuditRepository extends JpaRepository<ApplicationAudit, String> {

    @Query(value = "select * "+
            "from audit.application_audit aa " +
            "where aa.event_date_time between :startTime and :endTime and (  'ALL' in (:authorList) or aa.username in ( :authorList ))", nativeQuery = true)
    List<ApplicationAudit> findAllChangesByDateAuthor(@Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("authorList") List<String> authorList);

    @Query("SELECT distinct aa.username FROM ApplicationAudit aa where aa.eventDateTime between :startTime and :endTime")
    List<String> findAllUsername(@Param("startTime") Long startTime, @Param("endTime") Long endTime);
}
