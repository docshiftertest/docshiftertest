package com.docshifter.core.monitoring.repositories;

import com.docshifter.core.monitoring.entities.MailTemplate;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by blazejm on 11.05.2017.
 */

//@Repository
public interface MailTemplateRepository extends CrudRepository<MailTemplate, Long> {
    List<MailTemplate> findByMailConfigurationItemId(long mailConfigurationItemId);
}
