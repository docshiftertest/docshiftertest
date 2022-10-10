package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.GlobalSettings;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by michiel.vandriessche@docbyte.com on 8/19/16.
 */

public interface GlobalSettingsRepository extends CrudRepository<GlobalSettings, Long> {


}
