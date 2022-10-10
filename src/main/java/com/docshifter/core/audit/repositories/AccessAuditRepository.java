package com.docshifter.core.audit.repositories;

import com.docshifter.core.audit.entities.AccessAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessAuditRepository extends JpaRepository<AccessAudit, String> {

}
