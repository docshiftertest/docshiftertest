package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.ApplicationAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationAuditRepository extends JpaRepository<ApplicationAudit, String> {
}
