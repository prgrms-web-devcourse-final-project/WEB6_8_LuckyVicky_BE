package com.back.domain.notification.integration;

import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 기존 테스트 데이터 사용 (TestInitData에서 생성됨)
        testUser = userRepository.findByEmail("user1@user.com")
                .orElseThrow(() -> new RuntimeException("테스트 사용자를 찾을 수 없습니다."));
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 알림만 삭제 (사용자는 TestInitData로 관리)
        notificationRepository.deleteAll();
    }

    @Test
    @DisplayName("알림 발송 및 조회 통합 테스트")
    void sendAndRetrieveNotification() {
        // when - 알림 발송
        notificationService.sendNotification(
                testUser,
                NotificationType.ORDER_CONFIRMED,
                "주문이 확정되었습니다. 주문번호: ORD12345",
                "/mypage/orders/1"
        );

        // then - 알림 저장 확인
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
        
        Notification saved = notifications.stream()
                .filter(n -> n.getMessage().contains("ORD12345"))
                .findFirst()
                .orElseThrow();
        
        assertThat(saved.getReceiver().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getType()).isEqualTo(NotificationType.ORDER_CONFIRMED);
        assertThat(saved.getMessage()).contains("ORD12345");
        assertThat(saved.getRelatedUrl()).isEqualTo("/mypage/orders/1");
        assertThat(saved.getIsRead()).isFalse();
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 통합 테스트")
    void countUnreadNotifications() {
        // given
        notificationService.sendNotification(testUser, NotificationType.ORDER_CONFIRMED, "메시지1", "/url1");
        notificationService.sendNotification(testUser, NotificationType.SHIPPING_STARTED, "메시지2", "/url2");
        notificationService.sendNotification(testUser, NotificationType.DELIVERY_COMPLETED, "메시지3", "/url3");

        // when
        long unreadCount = notificationService.getUnreadCount(testUser);

        // then
        assertThat(unreadCount).isEqualTo(3);
    }

    @Test
    @DisplayName("알림 읽음 처리 후 개수 감소 확인")
    void markAsReadAndCountUnread() {
        // given
        notificationService.sendNotification(testUser, NotificationType.ORDER_CONFIRMED, "메시지1", "/url1");
        notificationService.sendNotification(testUser, NotificationType.SHIPPING_STARTED, "메시지2", "/url2");
        
        List<Notification> notifications = notificationRepository.findAll();
        Long firstNotificationId = notifications.stream()
                .filter(n -> n.getReceiver().equals(testUser))
                .findFirst()
                .orElseThrow()
                .getId();

        // when - 첫 번째 알림 읽음 처리
        notificationService.markAsRead(testUser, firstNotificationId);

        // then
        long unreadCount = notificationService.getUnreadCount(testUser);
        assertThat(unreadCount).isEqualTo(1);
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 통합 테스트")
    void markAllAsRead() {
        // given
        notificationService.sendNotification(testUser, NotificationType.ORDER_CONFIRMED, "메시지1", "/url1");
        notificationService.sendNotification(testUser, NotificationType.SHIPPING_STARTED, "메시지2", "/url2");
        notificationService.sendNotification(testUser, NotificationType.DELIVERY_COMPLETED, "메시지3", "/url3");

        // when
        int markedCount = notificationService.markAllAsRead(testUser);

        // then
        assertThat(markedCount).isEqualTo(3);
        long unreadCount = notificationService.getUnreadCount(testUser);
        assertThat(unreadCount).isZero();
    }

    @Test
    @DisplayName("알림 삭제 통합 테스트")
    void deleteNotification() {
        // given
        notificationService.sendNotification(testUser, NotificationType.ORDER_CONFIRMED, "메시지1", "/url1");
        notificationService.sendNotification(testUser, NotificationType.SHIPPING_STARTED, "메시지2", "/url2");
        
        List<Notification> beforeDelete = notificationRepository.findAll();
        int beforeCount = (int) beforeDelete.stream()
                .filter(n -> n.getReceiver().equals(testUser))
                .count();
        
        Long firstNotificationId = beforeDelete.stream()
                .filter(n -> n.getReceiver().equals(testUser))
                .findFirst()
                .orElseThrow()
                .getId();

        // when
        notificationService.deleteNotification(testUser, firstNotificationId);

        // then
        List<Notification> afterDelete = notificationRepository.findAll();
        int afterCount = (int) afterDelete.stream()
                .filter(n -> n.getReceiver().equals(testUser))
                .count();
        
        assertThat(afterCount).isEqualTo(beforeCount - 1);
    }

    @Test
    @DisplayName("다수의 사용자에게 알림 발송 테스트")
    void sendNotificationsToMultipleUsers() {
        // given - 기존 테스트 데이터 사용
        User user1 = userRepository.findByEmail("user1@user.com").orElseThrow();
        User user2 = userRepository.findByEmail("user2@user.com").orElseThrow();
        User artist1 = userRepository.findByEmail("artist1@artist.com").orElseThrow();

        // when - 각 사용자에게 알림 발송
        notificationService.sendNotification(user1, NotificationType.FUNDING_SUCCESS, "펀딩 성공", "/funding/1");
        notificationService.sendNotification(user2, NotificationType.FUNDING_SUCCESS, "펀딩 성공", "/funding/1");
        notificationService.sendNotification(artist1, NotificationType.FUNDING_SUCCESS, "펀딩 성공", "/funding/1");

        // then
        assertThat(notificationService.getUnreadCount(user1)).isGreaterThanOrEqualTo(1);
        assertThat(notificationService.getUnreadCount(user2)).isGreaterThanOrEqualTo(1);
        assertThat(notificationService.getUnreadCount(artist1)).isGreaterThanOrEqualTo(1);
    }
}
