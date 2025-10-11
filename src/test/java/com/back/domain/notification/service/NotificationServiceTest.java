package com.back.domain.notification.service;

import com.back.domain.notification.dto.NotificationResponse;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.repository.NotificationRepository;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("알림 발송 성공")
    void sendNotification_Success() {
        // given
        User receiver = createUser(1L);
        NotificationType type = NotificationType.ORDER_CONFIRMED;
        String message = "주문이 확정되었습니다.";
        String relatedUrl = "/mypage/orders/1";

        given(notificationRepository.save(any(Notification.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        notificationService.sendNotification(receiver, type, message, relatedUrl);

        // then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 목록 조회 성공")
    void getNotifications_Success() {
        // given
        User user = createUser(1L);
        Pageable pageable = PageRequest.of(0, 20);
        Notification notification = createNotification(1L, user, NotificationType.ORDER_CONFIRMED);
        Page<Notification> notificationPage = new PageImpl<>(List.of(notification));

        given(notificationRepository.findByReceiverOrderByCreateDateDesc(user, pageable))
                .willReturn(notificationPage);

        // when
        Page<NotificationResponse> result = notificationService.getNotifications(user, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).type()).isEqualTo(NotificationType.ORDER_CONFIRMED);
        verify(notificationRepository).findByReceiverOrderByCreateDateDesc(user, pageable);
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 성공")
    void getUnreadCount_Success() {
        // given
        User user = createUser(1L);
        given(notificationRepository.countByReceiverAndIsReadFalse(user)).willReturn(5L);

        // when
        long result = notificationService.getUnreadCount(user);

        // then
        assertThat(result).isEqualTo(5L);
        verify(notificationRepository).countByReceiverAndIsReadFalse(user);
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void markAsRead_Success() {
        // given
        User user = createUser(1L);
        Notification notification = createNotification(1L, user, NotificationType.ORDER_CONFIRMED);
        
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        // when
        notificationService.markAsRead(user, 1L);

        // then
        assertThat(notification.getIsRead()).isTrue();
        verify(notificationRepository).findById(1L);
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 알림 없음")
    void markAsRead_NotFound() {
        // given
        User user = createUser(1L);
        given(notificationRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(user, 1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("알림을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - 권한 없음")
    void markAsRead_Forbidden() {
        // given
        User user = createUser(1L);
        User otherUser = createUser(2L);
        Notification notification = createNotification(1L, otherUser, NotificationType.ORDER_CONFIRMED);
        
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(user, 1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("본인의 알림만 읽을 수 있습니다");
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 성공")
    void markAllAsRead_Success() {
        // given
        User user = createUser(1L);
        given(notificationRepository.markAllAsReadByReceiver(user)).willReturn(3);

        // when
        int result = notificationService.markAllAsRead(user);

        // then
        assertThat(result).isEqualTo(3);
        verify(notificationRepository).markAllAsReadByReceiver(user);
    }

    @Test
    @DisplayName("알림 삭제 성공")
    void deleteNotification_Success() {
        // given
        User user = createUser(1L);
        Notification notification = createNotification(1L, user, NotificationType.ORDER_CONFIRMED);
        
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));
        doNothing().when(notificationRepository).delete(notification);

        // when
        notificationService.deleteNotification(user, 1L);

        // then
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).delete(notification);
    }

    @Test
    @DisplayName("알림 삭제 실패 - 알림 없음")
    void deleteNotification_NotFound() {
        // given
        User user = createUser(1L);
        given(notificationRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.deleteNotification(user, 1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("알림을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("알림 삭제 실패 - 권한 없음")
    void deleteNotification_Forbidden() {
        // given
        User user = createUser(1L);
        User otherUser = createUser(2L);
        Notification notification = createNotification(1L, otherUser, NotificationType.ORDER_CONFIRMED);
        
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        // when & then
        assertThatThrownBy(() -> notificationService.deleteNotification(user, 1L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("본인의 알림만 삭제할 수 있습니다");
    }

    // ==================== Helpers ====================

    private User createUser(Long id) {
        User user = mock(User.class);
        lenient().when(user.getId()).thenReturn(id);
        return user;
    }

    private Notification createNotification(Long id, User receiver, NotificationType type) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .type(type)
                .message("테스트 메시지")
                .relatedUrl("/test/url")
                .build();
        ReflectionTestUtils.setField(notification, "id", id);
        return notification;
    }
}
