package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.ApplicationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationAuditRepository extends JpaRepository<ApplicationAudit, String> {

    @Query(value = "select * "+
            "from audit.application_audit aa " +
            "where aa.event_date_time between :startTime and :endTime and (  'ALL' in (:authorList) or aa.username in ( :authorList ))", nativeQuery = true)
    List<ApplicationAudit> findAllChangesByDateAuthor(@Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("authorList") List<String> authorList);

    @Query(value = "SELECT distinct aa.username FROM application_audit aa where aa.event_date_time between ?startTime and ?endTime", nativeQuery = true)
    List<String> findAllUsername(@Param("startTime") Long startTime, @Param("endTime") Long endTime);
}
