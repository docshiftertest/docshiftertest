package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.WorkflowAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowAuditRepository extends JpaRepository<WorkflowAudit, String> {

    @Query(value = "select * " +
            "from audit.workflow_audit wa " +
            "where wa.event_date_time between :startTime and :endTime and (  'ALL' in (:authorList) or wa.username in ( :authorList ))",nativeQuery = true)
    List<WorkflowAudit> findAllChangesByDateAuthor(@Param("startTime") Long startTime, @Param("endTime") Long endTime, @Param("authorList") List<String> authorList);

    @Query(value = "SELECT distinct wa.username FROM audit.workflow_audit wa where wa.event_date_time between :startTime and :endTime ", nativeQuery = true)
    List<String> findAllUsername(@Param("startTime") Long startTime, @Param("endTime") Long endTime);
}
