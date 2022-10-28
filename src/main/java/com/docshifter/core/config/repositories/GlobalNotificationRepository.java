package com.docshifter.core.config.repositories;

import com.docshifter.core.config.entities.GlobalNotification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

@Repository
public interface GlobalNotificationRepository extends CrudRepository<GlobalNotification, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT gn FROM GlobalNotification gn WHERE gn.id = ?1")
    GlobalNotification findOnePessimistic(Long id);
}
