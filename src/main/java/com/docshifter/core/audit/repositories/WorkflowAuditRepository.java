package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.WorkflowAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowAuditRepository extends JpaRepository<WorkflowAudit, Long> {
}
