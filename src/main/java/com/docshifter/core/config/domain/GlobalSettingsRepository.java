package com.docshifter.core.config.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */
@Repository
public interface GlobalSettingsRepository extends CrudRepository<GlobalSettings, Long> {


}