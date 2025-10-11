package com.back.domain.notification.repository;

import com.back.domain.notification.entity.Notification;
import com.back.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 알림 목록 조회 (페이징, 최신순)
     */
    Page<Notification> findByReceiverOrderByCreateDateDesc(User receiver, Pageable pageable);

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    long countByReceiverAndIsReadFalse(User receiver);

    /**
     * 사용자의 모든 알림 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver = :receiver AND n.isRead = false")
    int markAllAsReadByReceiver(@Param("receiver") User receiver);
}
