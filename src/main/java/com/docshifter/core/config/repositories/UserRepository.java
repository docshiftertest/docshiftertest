package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value="select * from docshifter.user users where users.email = :usernameOrEmail or users.username = :usernameOrEmail", nativeQuery = true)
    User findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    @Query(value = "select * from docshifter.user as users" +
            " inner join docshifter.user_roles as user_role on user_role.user_id = users.id" +
            " inner join docshifter.role as role on role.id = user_role.role_id" +
            " where role.display_name in(:rolesNames)", nativeQuery = true)
    List<User> findAllByRoles( @Param("rolesNames") List<String> rolesNames);


    @Query(value="select * from docshifter.user users where users.email = :usernameOrEmail or users.username = :usernameOrEmail", nativeQuery = true)
    List<User> findAllByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    @Query(value = "delete from docshifter.user where username in (:usernameList)", nativeQuery = true)
    @Modifying
    void deleteAllByUsername( @Param("usernameList") Set<String> usernameList);


}
