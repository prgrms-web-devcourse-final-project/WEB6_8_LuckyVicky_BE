package com.back.domain.notification.service;

import com.back.domain.notification.dto.NotificationResponse;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성 및 발송
     */
    @Transactional
    public void sendNotification(User receiver, NotificationType type, String message, String relatedUrl) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .type(type)
                .message(message)
                .relatedUrl(relatedUrl)
                .build();

        notificationRepository.save(notification);
        log.info("알림 발송 완료 - receiverId: {}, type: {}, message: {}", 
                receiver.getId(), type, message);
    }

    /**
     * 사용자의 알림 목록 조회 (페이징)
     */
    public Page<NotificationResponse> getNotifications(User user, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByReceiverOrderByCreateDateDesc(user, pageable);
        return notifications.map(NotificationResponse::from);
    }

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    public long getUnreadCount(User user) {
        return notificationRepository.countByReceiverAndIsReadFalse(user);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ServiceException("404", "알림을 찾을 수 없습니다."));

        // 권한 체크
        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new ServiceException("403", "본인의 알림만 읽을 수 있습니다.");
        }

        notification.markAsRead();
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsReadByReceiver(user);
    }

    /**
     * 알림 삭제
     */
    @Transactional
    public void deleteNotification(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ServiceException("404", "알림을 찾을 수 없습니다."));

        // 권한 체크
        if (!notification.getReceiver().getId().equals(user.getId())) {
            throw new ServiceException("403", "본인의 알림만 삭제할 수 있습니다.");
        }

        notificationRepository.delete(notification);
    }
}
