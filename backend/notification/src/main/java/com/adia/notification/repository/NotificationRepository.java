package com.adia.notification.repository;

import com.adia.notification.Notification;
import com.adia.notification.entity.NotificationEntity;
import com.adia.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByIdOrderByCreatedAtDesc(Long userId);
    List<NotificationEntity> findByReceiverIdOrderByCreatedAtDesc(String userId);
    List<NotificationEntity> findUnreadByReceiverId(String userId);
}
