package com.proga.notification_service.repository;

import com.proga.notification_service.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Short> {

    Optional<NotificationType> findByCode(String code);
}
