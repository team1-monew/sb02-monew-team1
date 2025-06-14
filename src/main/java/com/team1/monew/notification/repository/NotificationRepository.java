package com.team1.monew.notification.repository;

import com.team1.monew.notification.entity.Notification;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("UPDATE Notification n " +
      " SET n.confirmed = true " +
      " WHERE n.user.id = :userId")
  void markAllAsConfirmedByUserId(@Param("userId") Long userId);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("UPDATE Notification n " +
      " SET n.confirmed = true " +
      " WHERE n.id = :notificationId")
  void markAsConfirmedByNotificationId(@Param("notificationId") Long notificationId);

  @Modifying
  @Transactional
  @Query("DELETE FROM Notification n " +
      " WHERE n.confirmed = true AND n.updatedAt < :cutoffDate")
  int deleteConfirmedBefore(LocalDateTime cutoffDate);

  long countByUserIdAndConfirmedFalse(Long userId);
}
