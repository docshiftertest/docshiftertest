package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.Properties;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertiesRepository extends CrudRepository<Properties, UUID> {

    @Query(value = "select * from docshifter.properties p where p.application = :application", nativeQuery = true)
    List<Properties> findByApplication(@Param("application") String application);

    @Modifying()
    @Query(value = "update docshifter.properties  set value = :value, enabled = :enabled where key = :key", nativeQuery = true)
    void updateProperty(@Param("key") String key,@Param("value") String value, @Param("enabled") boolean enabled);


}
