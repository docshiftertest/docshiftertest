package com.docshifter.core.monitoring.repositories;

import com.docshifter.core.monitoring.entities.MailTemplate;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by blazejm on 11.05.2017.
 */

//@Repository
@JaversSpringDataAuditable
public interface MailTemplateRepository extends CrudRepository<MailTemplate, Long> {
    List<MailTemplate> findByMailConfigurationItemId(long mailConfigurationItemId);
}
