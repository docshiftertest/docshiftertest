package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UserRolesRepository extends JpaRepository<Role, Long> {


    @Query(value = "select distinct display_name from docshifter.role", nativeQuery = true)
    Set<String> findAllRolesName();

}
